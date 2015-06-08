package eu.vranckaert.heart.rate.monitor.controller;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import eu.vranckaert.hear.rate.monitor.shared.model.ActivityState;
import eu.vranckaert.heart.rate.monitor.BusinessService;
import eu.vranckaert.heart.rate.monitor.UserPreferences;

import java.util.List;

/**
 * Date: 01/06/15
 * Time: 08:36
 *
 * @author Dirk Vranckaert
 */
public class ActivityRecognitionIntentService extends IntentService {
    public ActivityRecognitionIntentService() {
        super(ActivityRecognitionIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            int confidence = mostProbableActivity.getConfidence();
            int activityType = mostProbableActivity.getType();

            if (activityType == DetectedActivity.UNKNOWN) {
                Log.d("dirk-background", "Current activity is unknown, trying to get a better activity");
                List<DetectedActivity> activityList = result.getProbableActivities();
                for (DetectedActivity activity : activityList) {
                    if (activity.getType() != activityType) {
                        activityType = activity.getType();
                        confidence = activity.getConfidence();
                    }
                }
            }

            String loggedActivity = "";
            switch (activityType) {
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

            Log.d("dirk-background", "Current activity = " + loggedActivity + "(confidence=" + confidence + ")");

            BusinessService.getInstance().setActivityUpdate(activityType);
        }
    }
}
