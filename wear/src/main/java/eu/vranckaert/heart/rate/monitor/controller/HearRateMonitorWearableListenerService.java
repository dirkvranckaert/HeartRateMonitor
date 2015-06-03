package eu.vranckaert.heart.rate.monitor.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import eu.vranckaert.hear.rate.monitor.shared.model.ActivityState;
import eu.vranckaert.hear.rate.monitor.shared.WearURL;
import eu.vranckaert.heart.rate.monitor.WearHeartRateApplication;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.WearUserPreferences;
import eu.vranckaert.heart.rate.monitor.service.AlarmSchedulingService;

/**
 * Date: 01/06/15
 * Time: 14:15
 *
 * @author Dirk Vranckaert
 */
public class HearRateMonitorWearableListenerService extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        Log.d("dirk", "Message received on " + path);

        if (path.startsWith(WearURL.URL_ACTIVITY_MONITORING_RESULT)) {
            Log.d("dirk", "Activity monitoring update");
            int currentActivity = Integer.parseInt(path.replace(WearURL.URL_ACTIVITY_MONITORING_RESULT, ""));

            boolean useActivity = true;
            switch (currentActivity) {
                case ActivityState.UNKNOWN:
                case ActivityState.TILTING:
                    useActivity = false;
            }

            Log.d("dirk", "Activity found is " + currentActivity + ", will ignore this activity? " + !useActivity);
            if (useActivity) {
                int previousActivity = WearUserPreferences.getInstance().getLatestActivity();
                WearUserPreferences.getInstance().storeLatestActivity(currentActivity);
                Log.d("dirk", "Activity is same as previous activity, so possible a trusted activity");
                if (currentActivity == previousActivity) {
                    int previousActivityCount = WearUserPreferences.getInstance().getLatestActivityCount();
                    int acceptedActivity = WearUserPreferences.getInstance().getAcceptedActivity();
                    Log.d("dirk", "Activity has been seen now for " + previousActivityCount + " times and the currentActivity is different from the acceptedActivity? " + (acceptedActivity != currentActivity));
                    if (previousActivityCount >= ActivityState.TRUSTED_COUNT && acceptedActivity != currentActivity) {
                        WearUserPreferences.getInstance().setAcceptedActivity(currentActivity);
                        test(currentActivity); // TODO remove the notification test block, it's only for testing!
                        if (ActivityState.getMeasuringIntervalForActivity(acceptedActivity) != ActivityState.getMeasuringIntervalForActivity(currentActivity)) {
                            AlarmSchedulingService.getInstance().rescheduleHeartRateMeasuringAlarms();
                        }
                    }
                }
            }
        }
    }

    private void test(int activity) {
        String loggedActivity = "";
        switch (activity) {
            case ActivityState.IN_VEHICLE:
                loggedActivity = "inVehicle";
                break;
            case ActivityState.ON_BICYCLE:
                loggedActivity = "onBicycle";
                break;
            case ActivityState.ON_FOOT:
                loggedActivity = "onFoot";
                break;
            case ActivityState.STILL:
                loggedActivity = "still";
                break;
            case ActivityState.UNKNOWN:
                loggedActivity = "unknown";
                break;
            case ActivityState.TILTING:
                loggedActivity = "tilting";
                break;
            case ActivityState.WALKING:
                loggedActivity = "walking";
                break;
            case ActivityState.RUNNING:
                loggedActivity = "running";
                break;
        }

        Notification notification = new Notification.Builder(WearHeartRateApplication.getContext())
                .setContentTitle("Activity Update")
                .setContentText("Current activity = " + loggedActivity)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        NotificationManager notificationManager =
                (NotificationManager) WearHeartRateApplication.getContext()
                        .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(101, notification);
    }
}
