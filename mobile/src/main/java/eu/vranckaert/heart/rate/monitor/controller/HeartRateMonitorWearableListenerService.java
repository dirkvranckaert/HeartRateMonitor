package eu.vranckaert.heart.rate.monitor.controller;

import android.Manifest.permission;
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
import com.google.android.gms.wearable.WearableListenerService;
import eu.vranckaert.heart.rate.monitor.BusinessService;
import eu.vranckaert.heart.rate.monitor.DebugUserPreferences;
import eu.vranckaert.heart.rate.monitor.FitHelper;
import eu.vranckaert.heart.rate.monitor.HeartRateApplication;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.UserPreferences;
import eu.vranckaert.heart.rate.monitor.shared.NotificationId;
import eu.vranckaert.heart.rate.monitor.shared.WearKeys;
import eu.vranckaert.heart.rate.monitor.shared.WearURL;
import eu.vranckaert.heart.rate.monitor.shared.dao.IMeasurementDao;
import eu.vranckaert.heart.rate.monitor.shared.dao.MeasurementDao;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.shared.permission.PermissionUtil;

import java.util.ArrayList;
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
        mDao = new MeasurementDao(this);
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
                if (WearURL.HEART_RATE_MEASUREMENTS.equals((path))) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    String measurementsJson = dataMap.getString(WearKeys.MEASUREMENTS);
                    List<Measurement> measurements = Measurement.fromJSONList(measurementsJson);
                    List<String> measurementUniqueKeys = new ArrayList<>();
                    int size = measurements.size();
                    Log.d("dirk", "number of measurements is " + size);
                    BusinessService businessService = BusinessService.getInstance();
                    for (int i = 0; i < size; i++) {
                        Measurement measurement = measurements.get(i);
                        measurement.setSyncedWithGoogleFit(false);
                        Log.d("dirk", "measurement=" + measurement.toJSON());
                        List<Measurement> matchingMeasurements = mDao.findUnique(measurement);
                        measurementUniqueKeys.add(measurement.getUniqueKey());
                        if (!matchingMeasurements.isEmpty()) {
                            Log.d("dirk", "This measurement is already in the database");
                            continue;
                        }

                        Log.d("dirk", "Storing the measurement locally");
                        mDao.save(measurement);
                    }

                    syncPostponedMeasurements();
                    businessService.sendHeartRateMeasurementsAck(measurementUniqueKeys);
                }
            }
        }
    }

    private void syncPostponedMeasurements() {
        boolean canSyncWithGoogleFit = DebugUserPreferences.getInstance().canSyncWithGoogleFit();
        if (!canSyncWithGoogleFit) {
            Log.d("dirk", "Skip Google Fit sync (disabled in dev settings)");
            return;
        }

        boolean hasPermissions = PermissionUtil.hasPermission(this, permission.BODY_SENSORS);
        if (!hasPermissions) {
            Log.d("dirk", "Skip Google Fit sync (missing permission " + permission.BODY_SENSORS + ")");
            return;
        }

        final BusinessService businessService = BusinessService.getInstance();
        final UserPreferences userPreferences = UserPreferences.getInstance();

        boolean hasAggregateHeartRateSummarySubscription =
                businessService.hasFitnessSubscription(FitHelper.DATA_TYPE_HEART_RATE);
        if (!hasAggregateHeartRateSummarySubscription) {
            userPreferences.setGoogleFitConnected(false);
            businessService.setPhoneSetupCompletionStatus(false);

            Log.d("dirk", "No Google Fitness subscriptions yet...");
            int errorCount = userPreferences.getGoogleFitActivationErrorCount();
            Log.d("dirk", "Google fit activation error occurred " + errorCount + " time(s) now");
            if (errorCount == 10) {
                Log.d("dirk", "Showing notification to logon to Google Fit");
                userPreferences.setGoogleFitActivationErrorCount(0);

                Intent intent = new Intent(HeartRateApplication.getContext(), MainActivity.class);
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(HeartRateApplication.getContext(), 0, intent, 0);

                Notification notification =
                        new Notification.Builder(HeartRateApplication.getContext())
                                .setContentTitle(HeartRateApplication.getContext()
                                        .getString(R.string.app_name))
                                .setContentText(HeartRateApplication.getContext()
                                        .getString(
                                                R.string.notification_google_fitness_not_connected_message))
                                .setStyle(
                                        new BigTextStyle().bigText(HeartRateApplication.getContext()
                                                .getString(
                                                        R.string.notification_google_fitness_not_connected_message)))
                                .setSmallIcon(R.drawable.ic_notification)
                                .setColor(HeartRateApplication.getContext().getResources()
                                        .getColor(R.color.hrm_accent_color))
                                .addAction(R.drawable.fit_notification,
                                        HeartRateApplication.getContext().getString(
                                                R.string.notification_google_fitness_not_connected_message_action_connect),
                                        pendingIntent)
                                .build();
                NotificationManager notificationManager = (NotificationManager) HeartRateApplication.getContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NotificationId.GOOGLE_FITNESS_NOT_CONNECTED, notification);
            } else {
                Log.d("dirk", "Not showing notification to logon to Google Fit right now");
                userPreferences.setGoogleFitActivationErrorCount(errorCount + 1);
            }
            return;
        }

        List<Measurement> measurements = mDao.findMeasurementsToSyncWithFit();
        Log.d("dirk", "Syncing all postponed measurements, found " + measurements.size() + " measurement(s)");
        for (int i = 0; i < measurements.size(); i++) {
            Measurement measurement = measurements.get(i);
            if (!measurement.isFakeHeartRate()) {
                UserPreferences.getInstance().setGoogleFitActivationErrorCount(-1);
                DataSet dataSet = getGoogleFitDataSet(measurement);
                boolean success = businessService.addFitnessHeartRateMeasurement(dataSet);
                Log.d("dirk", "Measurement from " + new Date(measurement.getStartMeasurement()) +
                        " has been synced with Google Fit. Result is success? " + success);
                if (success) {
                    Log.d("dirk", "Storing the BPM in Google Fit was successful, updating local DB");
                    measurement.setSyncedWithGoogleFit(true);
                    mDao.update(measurement);
                } else {
                    Log.d("dirk", "Could not store BPM in Google Fit, updating local DB");
                    measurement.setSyncedWithGoogleFit(false);
                    mDao.update(measurement);
                }
            } else {
                Log.d("dirk", "Fake heart rate! Reason:" + measurement.detectFakeHeartRate());
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
        dataPoint.setTimeInterval(measurement.getStartMeasurement(), measurement.getEndMeasurement(),
                TimeUnit.MILLISECONDS);
        //dataPoint.getValue(Field.FIELD_MIN).setFloat(measurement.getMinimumHeartBeat());
        dataPoint.getValue(Field.FIELD_BPM).setFloat(measurement.getAverageHeartBeat());
        //dataPoint.getValue(Field.FIELD_AVERAGE).setFloat(measurement.getAverageHeartBeat());
        //dataPoint.getValue(Field.FIELD_MAX).setFloat(measurement.getMaximumHeartBeat());
        dataSet.add(dataPoint);
        return dataSet;
    }
}
