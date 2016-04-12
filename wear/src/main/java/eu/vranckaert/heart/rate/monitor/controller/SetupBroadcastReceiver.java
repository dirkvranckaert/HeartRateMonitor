package eu.vranckaert.heart.rate.monitor.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import eu.vranckaert.heart.rate.monitor.service.AlarmSchedulingService;

/**
 * Date: 01/06/15
 * Time: 18:02
 *
 * @author Dirk Vranckaert
 */
public class SetupBroadcastReceiver extends BroadcastReceiver {
    public static void setupMeasuring(Context context) {
        AlarmSchedulingService.getInstance().rescheduleHeartRateMeasuringAlarms(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        setupMeasuring(context);
    }
}
