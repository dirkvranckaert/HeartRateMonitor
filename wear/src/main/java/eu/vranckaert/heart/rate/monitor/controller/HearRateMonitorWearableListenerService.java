package eu.vranckaert.heart.rate.monitor.controller;

import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import eu.vranckaert.hear.rate.monitor.shared.WearURL;
import eu.vranckaert.hear.rate.monitor.shared.model.ActivityState;
import eu.vranckaert.heart.rate.monitor.WearUserPreferences;
import eu.vranckaert.heart.rate.monitor.service.AlarmSchedulingService;
import eu.vranckaert.heart.rate.monitor.task.ActivitySetupTask;

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

        final WearUserPreferences preferences = WearUserPreferences.getInstance();
        switch (path) {
            case WearURL.URL_ACTIVITY_MONITORING_RESULT:
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
                    int previousActivity = preferences.getLatestActivity();
                    preferences.storeLatestActivity(currentActivity);
                    Log.d("dirk", "Activity is same as previous activity, so possible a trusted activity");
                    if (currentActivity == previousActivity) {
                        int previousActivityCount = preferences.getLatestActivityCount();
                        int acceptedActivity = preferences.getAcceptedActivity();
                        Log.d("dirk", "Activity has been seen now for " + previousActivityCount +
                                " times and the currentActivity is different from the acceptedActivity? " +
                                (acceptedActivity != currentActivity));
                        if (previousActivityCount >= ActivityState.TRUSTED_FACTOR &&
                                acceptedActivity != currentActivity) {
                            preferences.setAcceptedActivity(currentActivity);
                            if (ActivityState.getMeasuringIntervalForActivity(acceptedActivity) !=
                                    ActivityState.getMeasuringIntervalForActivity(currentActivity)) {
                                AlarmSchedulingService.getInstance().rescheduleHeartRateMeasuringAlarms();
                            }
                        }
                    }
                }
                break;
            case WearURL.URL_SETUP_COMPLETED:
                preferences.setPhoneSetupCompleted(true);
                preferences.setHasntRunBefore();
                new ActivitySetupTask().execute();
                SetupBroadcastReceiver.setupMeasuring();
                break;
            case WearURL.URL_SETUP_UNCOMPLETED:
                preferences.setPhoneSetupCompleted(false);
                preferences.setHasntRunBefore();
                break;
        }
    }
}
