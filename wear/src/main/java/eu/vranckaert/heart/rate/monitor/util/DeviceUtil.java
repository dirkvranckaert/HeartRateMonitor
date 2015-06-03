package eu.vranckaert.heart.rate.monitor.util;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import eu.vranckaert.heart.rate.monitor.WearHeartRateApplication;

/**
 * Date: 03/06/15
 * Time: 08:06
 *
 * @author Dirk Vranckaert
 */
public class DeviceUtil {
    public static boolean isCharging() {
        boolean isCharging;
        Intent intent = WearHeartRateApplication.getContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        isCharging = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB ||
                plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        return isCharging;
    }
}
