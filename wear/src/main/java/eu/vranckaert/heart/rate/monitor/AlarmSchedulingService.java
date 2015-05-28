package eu.vranckaert.heart.rate.monitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Date: 04/02/15
 * Time: 14:23
 *
 * @author Dirk Vranckaert
 */
public class AlarmSchedulingService {
    private static final int REQUEST_CODE_ONE_TIME_HEART_RATE_MEASUREMENT = 322;

    private static AlarmSchedulingService INSTANCE;

    public static AlarmSchedulingService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AlarmSchedulingService();
        }

        return INSTANCE;
    }

    private static AlarmManager getAlarmManager() {
        return (AlarmManager) HearRateApplication.getInstance().getApplicationContext().getSystemService(
                Context.ALARM_SERVICE);
    }

    private PendingIntent getHeartRateMonitorIntent() {
        Context context = HearRateApplication.getInstance().getApplicationContext();
        Intent intent = new Intent(context, HeartRateMonitorIntentService.class);
        PendingIntent operation = PendingIntent.getService(context, REQUEST_CODE_ONE_TIME_HEART_RATE_MEASUREMENT, intent, 0);
        return operation;
    }

    public void scheduleHeartRateMonitorInXMillis(int delay) {
        getAlarmManager().cancel(getHeartRateMonitorIntent());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, delay);

        Log.i("dirk", "Heart rate monitoring will be started in one minute, at " + calendar.getTime().toString());

        getAlarmManager().setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), getHeartRateMonitorIntent());
    }
}
