package eu.vranckaert.heart.rate.monitor.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import eu.vranckaert.hear.rate.monitor.shared.model.ActivityState;
import eu.vranckaert.heart.rate.monitor.WearUserPreferences;
import eu.vranckaert.heart.rate.monitor.service.AlarmSchedulingService;

/**
 * Date: 01/06/15
 * Time: 18:02
 *
 * @author Dirk Vranckaert
 */
public class SetupBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        setupMeasuring();
    }

    public static void setupMeasuring() {
        WearUserPreferences.getInstance().storeLatestActivity(ActivityState.STILL);
        WearUserPreferences.getInstance().setAcceptedActivity(ActivityState.STILL);
        if (WearUserPreferences.getInstance().isPhoneSetupCompleted()) {
            AlarmSchedulingService.getInstance().rescheduleHeartRateMeasuringAlarms();
        }
    }
}
