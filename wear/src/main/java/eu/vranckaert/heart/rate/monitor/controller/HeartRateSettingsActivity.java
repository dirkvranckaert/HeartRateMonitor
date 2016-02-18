package eu.vranckaert.heart.rate.monitor.controller;

import android.os.Bundle;
import android.util.Log;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.WearUserPreferences;
import eu.vranckaert.heart.rate.monitor.service.AlarmSchedulingService;
import preference.WearPreferenceActivity;

/**
 * Date: 17/02/16
 * Time: 15:51
 *
 * @author Dirk Vranckaert
 */
public class HeartRateSettingsActivity extends WearPreferenceActivity {
    private WearUserPreferences userPreferences;

    private long previousMeasuringInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userPreferences = WearUserPreferences.getInstance();
        previousMeasuringInterval = userPreferences.getHeartRateMeasuringInterval();

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void finish() {
        long heartRateMeasuringInterval = userPreferences.getHeartRateMeasuringInterval();
        if (heartRateMeasuringInterval != previousMeasuringInterval) {
            Log.d("dirk", "heartRateMeasuringInterval has changed, reschedule now!");
            AlarmSchedulingService.getInstance().rescheduleHeartRateMeasuringAlarms(this);
        }

        Log.d("dirk", "Can finish after processing preference changes");
        super.finish();
    }
}
