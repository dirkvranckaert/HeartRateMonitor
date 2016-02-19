package eu.vranckaert.heart.rate.monitor;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Date: 01/06/15
 * Time: 17:27
 *
 * @author Dirk Vranckaert
 */
public class DebugUserPreferences {
    private static final String KEY_SYNC_GOOGLE_FIT = "debug_sync_with_google_fit";

    private static DebugUserPreferences INSTANCE;

    private final SharedPreferences mSharedPreferences;
    private final Editor mEditor;

    private DebugUserPreferences() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(HeartRateApplication.getContext());
        mEditor = mSharedPreferences.edit();
    }

    public static DebugUserPreferences getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DebugUserPreferences();
        }
        return INSTANCE;
    }

    public boolean canSyncWithGoogleFit() {
        return mSharedPreferences.getBoolean(KEY_SYNC_GOOGLE_FIT, true);
    }
}
