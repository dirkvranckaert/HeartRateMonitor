package eu.vranckaert.heart.rate.monitor.controller;

import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import eu.vranckaert.hear.rate.monitor.shared.NotificationId;
import eu.vranckaert.hear.rate.monitor.shared.WearKeys;
import eu.vranckaert.hear.rate.monitor.shared.WearURL;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.BusinessService;
import eu.vranckaert.heart.rate.monitor.FitHelper;
import eu.vranckaert.heart.rate.monitor.HeartRateApplication;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.UserPreferences;
import eu.vranckaert.heart.rate.monitor.dao.IMeasurementDao;
import eu.vranckaert.heart.rate.monitor.dao.MeasurementDao;
import eu.vranckaert.heart.rate.monitor.task.ActivityRecognitionTask;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Date: 01/06/15
 * Time: 14:15
 *
 * @author Dirk Vranckaert
 */
public class HeartRateMonitorWearableListenerService extends WearableListenerService {
    private IMeasurementDao mDao;

    @Override
    public void onCreate() {
        super.onCreate();
        mDao = new MeasurementDao();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        Log.d("dirk", "Message received on " + path);

        if (WearURL.URL_START_ACTIVITY_MONITORING.equals(path)) {
            new ActivityRecognitionTask().execute();
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("dirk", "On data changed...");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                String path = item.getUri().getPath();
                Log.d("dirk", "... on path " + path);
                if (WearURL.HEART_RATE_MEASUREMENT.equals((path))) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    String measurementJson = dataMap.getString(WearKeys.MEASUREMENT);
                    Measurement measurement = Measurement.fromJSON(measurementJson);
                    measurement.setSyncedWithGoogleFit(false);
                    Log.d("dirk", "measurement=" + measurement.toJSON());
                    List<Measurement> matchingMeasurements = mDao.findExact(measurement);
                    if (!matchingMeasurements.isEmpty()) {
                        Log.d("dirk", "This measurement is already in the database");
                        return;
                    }

                    Log.d("dirk", "Storing the measurement locally");
                    measurement = mDao.save(measurement);

                    boolean hasAggregateHeartRateSummarySubscription =
                            BusinessService.getInstance().hasFitnessSubscription(FitHelper.DATA_TYPE_HEART_RATE);
                    Log.d("dirk", "hasAggregateHeartRateSummarySubscription=" + hasAggregateHeartRateSummarySubscription);
                    hasAggregateHeartRateSummarySubscription = false; // TODO enable again to enable all Fit synchronisation
                    if (hasAggregateHeartRateSummarySubscription) {
                        UserPreferences.getInstance().setGoogleFitActivationErrorCount(-1);
                        DataSet dataSet = getGoogleFitDataSet(measurement);

                        Log.d("dirk", "Storing Google Fitness BPM DataSet");
                        boolean success = BusinessService.getInstance().addFitnessHeartRateMeasurement(dataSet);
                        if (success) {
                            Log.d("dirk", "Storing the BPM in Google Fit was successful, updating local DB");
                            measurement.setSyncedWithGoogleFit(true);
                            mDao.update(measurement);

                            syncPostponedMeasurements();
                        } else {
                            Log.d("dirk", "Could not store BPM in Google Fit, updating local DB");
                            measurement.setSyncedWithGoogleFit(false);
                            mDao.update(measurement);
                        }
                    } else {
                        Log.d("dirk", "No Google Fitness subscriptions yet...");

                        int errorCount = UserPreferences.getInstance().getGoogleFitActivationErrorCount();
                        if (errorCount == 10) {
                            UserPreferences.getInstance().setGoogleFitActivationErrorCount(0);

                            Intent intent = new Intent(HeartRateApplication.getContext(), GoogleFitSetupActivity.class);
                            PendingIntent pendingIntent = PendingIntent.getActivity(HeartRateApplication.getContext(), 0, intent, 0);

                            Notification notification = new Notification.Builder(HeartRateApplication.getContext())
                                    .setContentTitle(HeartRateApplication.getContext()
                                            .getString(R.string.app_name))
                                    .setContentText(HeartRateApplication.getContext()
                                            .getString(R.string.notification_google_fitness_not_connected_message))
                                    .setStyle(new BigTextStyle().bigText(HeartRateApplication.getContext()
                                            .getString(R.string.notification_google_fitness_not_connected_message)))
                                    .setSmallIcon(R.drawable.ic_notification)
                                    .setColor(HeartRateApplication.getContext().getResources().getColor(R.color.hrm_accent_color))
                                    .addAction(R.drawable.fit_notification,
                                            HeartRateApplication.getContext().getString(R.string.notification_google_fitness_not_connected_message_action_connect),
                                            pendingIntent)
                                    .build();
                            NotificationManager notificationManager =
                                    (NotificationManager) HeartRateApplication.getContext()
                                            .getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.notify(NotificationId.GOOGLE_FITNESS_NOT_CONNECTED, notification);
                        } else {
                            UserPreferences.getInstance().setGoogleFitActivationErrorCount(errorCount + 1);
                        }
                    }
                }
            }
        }
    }

    private void syncPostponedMeasurements() {
        List<Measurement> measurements = mDao.findMeasurementsToSync();
        Log.d("dirk", "Sycning all postponed measurements, found " + measurements.size() + " measurement(s)");
        for (int i=0; i<measurements.size(); i++) {
            Measurement measurement = measurements.get(i);
            DataSet dataSet = getGoogleFitDataSet(measurement);
            boolean succuess = BusinessService.getInstance().addFitnessHeartRateMeasurement(dataSet);
            Log.d("dirk", "Measurement from " + new Date(measurement.getStartMeasurement()) + " has been synced with Google Fit. Result is success? " + succuess);
            if (succuess) {
                Log.d("dirk", "Updating local storage indicating the measurement should be in Google Fit");
                measurement.setSyncedWithGoogleFit(true);
                mDao.update(measurement);
            }
        }
    }

    @NonNull
    private DataSet getGoogleFitDataSet(Measurement measurement) {
        Log.d("dirk", "Building DataSource");
        DataSource dataSource = new DataSource.Builder()
                .setDataType(FitHelper.DATA_TYPE_HEART_RATE)
                .setType(DataSource.TYPE_RAW)
                .setAppPackageName(HeartRateApplication.getContext())
                .build();
        Log.d("dirk", "Building DataSet");
        DataSet dataSet = DataSet.create(dataSource);
        Log.d("dirk", "Building DataPoint");
        DataPoint dataPoint = dataSet.createDataPoint();
        dataPoint.setTimeInterval(measurement.getStartMeasurement(), measurement.getEndMeasurement(), TimeUnit.MILLISECONDS);
        //dataPoint.getValue(Field.FIELD_MIN).setFloat(measurement.getMinimumHeartBeat());
        dataPoint.getValue(Field.FIELD_BPM).setFloat(measurement.getAverageHeartBeat());
        //dataPoint.getValue(Field.FIELD_AVERAGE).setFloat(measurement.getAverageHeartBeat());
        //dataPoint.getValue(Field.FIELD_MAX).setFloat(measurement.getMaximumHeartBeat());
        dataSet.add(dataPoint);
        return dataSet;
    }
}
