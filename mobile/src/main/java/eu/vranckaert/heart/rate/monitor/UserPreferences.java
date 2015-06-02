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
public class UserPreferences {
    private static final String KEY_LATEST_ACTIVITY = "latest_activity";

    private static UserPreferences INSTANCE;
    
    private final SharedPreferences mSharedPreferences;
    private final Editor mEditor;
    
    private UserPreferences() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(HearRateApplication.getContext());
        mEditor = mSharedPreferences.edit();
    }
    
    public static UserPreferences getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserPreferences();
        }
        return INSTANCE;
    }

    public void storeLatestActivity(int activityState) {
        mEditor.putInt(KEY_LATEST_ACTIVITY, activityState);
        mEditor.commit();
    }

    public int getLatestActivity() {
        return mSharedPreferences.getInt(KEY_LATEST_ACTIVITY, -1);
    }
}
