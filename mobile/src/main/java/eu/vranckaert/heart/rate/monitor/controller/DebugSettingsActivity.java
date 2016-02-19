package eu.vranckaert.heart.rate.monitor.controller;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import eu.vranckaert.heart.rate.monitor.R;

/**
 * Date: 19/02/16
 * Time: 06:26
 *
 * @author Dirk Vranckaert
 */
public class DebugSettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.debug_settings_title);

        addPreferencesFromResource(R.xml.debug_settings);
    }
}
