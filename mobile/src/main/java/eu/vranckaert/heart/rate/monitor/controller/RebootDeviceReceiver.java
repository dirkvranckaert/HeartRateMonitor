package eu.vranckaert.heart.rate.monitor.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import eu.vranckaert.heart.rate.monitor.task.ActivityRecognitionTask;

/**
 * Date: 01/06/15
 * Time: 18:02
 *
 * @author Dirk Vranckaert
 */
public class RebootDeviceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Make sure we are still getting activity updates
        new ActivityRecognitionTask().execute();
    }
}
