package eu.vranckaert.heart.rate.monitor.controller;

import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.HistoryApi;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
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
                            BusinessService.getInstance().hasFitnessSubscription(
                                    DataType.AGGREGATE_HEART_RATE_SUMMARY);
                    Log.d("dirk", "hasAggregateHeartRateSummarySubscription=" + hasAggregateHeartRateSummarySubscription);
                    if (hasAggregateHeartRateSummarySubscription) {
                        DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                        String measurementJson = dataMap.getString(WearKeys.MEASUREMENT);
                        Measurement measurement = Measurement.fromJSON(measurementJson);

                        Log.d("dirk", "measurement=" + measurement.toJSON());

                        Log.d("dirk", "Building DataSource");
                        DataSource dataSource = new DataSource.Builder()
                                .setDataType(DataType.AGGREGATE_HEART_RATE_SUMMARY)
                                .setType(DataSource.TYPE_RAW)
                                .build();
                        Log.d("dirk", "Building DataPoint");
                        DataPoint dataPoint = DataPoint.create(dataSource);
                        dataPoint.setTimeInterval(measurement.getStartMeasurement(), measurement.getEndMeasurement(),
                                TimeUnit.MILLISECONDS);
                        dataPoint.getValue(Field.FIELD_MIN).setFloat(measurement.getMinimumHeartBeat());
                        dataPoint.getValue(Field.FIELD_AVERAGE).setFloat(measurement.getAverageHeartBeat());
                        dataPoint.getValue(Field.FIELD_MAX).setFloat(measurement.getMaximumHeartBeat());
                        Log.d("dirk", "Building DataSet");
                        DataSet dataSet = DataSet.create(dataSource);
                        dataSet.add(dataPoint);

                        Log.d("dirk", "Storing Google Fitness BPM DataSet");
                        // TODO remove this notification
                        Notification notification = new Notification.Builder(HeartRateApplication.getContext())
                                .setContentTitle(HeartRateApplication.getContext().getString(R.string.app_name))
                                .setContentText("Will add the measurement to Google Fit")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .build();
                        NotificationManager notificationManager =
                                (NotificationManager) HeartRateApplication.getContext()
                                        .getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(5, notification);
                        //BusinessService.getInstance().addFitnessHeartRateMeasurement(dataSet);
                    } else {
                        Log.d("dirk", "No Google Fitness subscriptions yet... Notify user...");
                        Notification notification = new Notification.Builder(HeartRateApplication.getContext())
                                .setContentTitle(HeartRateApplication.getContext()
                                        .getString(R.string.app_name))
                                .setContentText(HeartRateApplication.getContext()
                                        .getString(R.string.notification_google_fitness_not_connected_message))
                                .setStyle(new BigTextStyle().bigText(HeartRateApplication.getContext()
                                        .getString(R.string.notification_google_fitness_not_connected_message)))
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .addAction(R.drawable.fit_notification, HeartRateApplication.getContext().getString(R.string.notification_google_fitness_not_connected_message_action_connect), null)
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
