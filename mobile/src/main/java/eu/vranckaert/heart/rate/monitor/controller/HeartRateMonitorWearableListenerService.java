package eu.vranckaert.heart.rate.monitor.controller;

import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import eu.vranckaert.heart.rate.monitor.task.ActivityRecognitionTask;

import java.util.concurrent.TimeUnit;

/**
 * Date: 01/06/15
 * Time: 14:15
 *
 * @author Dirk Vranckaert
 */
public class HeartRateMonitorWearableListenerService extends WearableListenerService {
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
                    boolean hasAggregateHeartRateSummarySubscription =
                            BusinessService.getInstance().hasFitnessSubscription(FitHelper.DATA_TYPE_HEART_RATE);
                    Log.d("dirk", "hasAggregateHeartRateSummarySubscription=" + hasAggregateHeartRateSummarySubscription);
                    if (hasAggregateHeartRateSummarySubscription) {
                        DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                        String measurementJson = dataMap.getString(WearKeys.MEASUREMENT);
                        Measurement measurement = Measurement.fromJSON(measurementJson);

                        Log.d("dirk", "measurement=" + measurement.toJSON());

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

                        Log.d("dirk", "Storing Google Fitness BPM DataSet");
                        // TODO remove this notification
                        Notification notification = new Notification.Builder(HeartRateApplication.getContext())
                                .setContentTitle(HeartRateApplication.getContext().getString(R.string.app_name))
                                .setContentText("Will add the measurement to Google Fit")
                                .setSmallIcon(R.drawable.ic_notification)
                                .setColor(HeartRateApplication.getContext().getResources()
                                .getColor(R.color.hrm_accent_color))
                                .build();
                        NotificationManager notificationManager =
                                (NotificationManager) HeartRateApplication.getContext()
                                        .getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(5, notification);
                        BusinessService.getInstance().addFitnessHeartRateMeasurement(dataSet);
                    } else {
                        Log.d("dirk", "No Google Fitness subscriptions yet... Notify user...");

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
                        // TODO add actions on the notification to start the registration activity
                    }
                }
            }
        }
    }
}
