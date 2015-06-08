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
    private static UserPreferences INSTANCE;
    
    private final SharedPreferences mSharedPreferences;
    private final Editor mEditor;
    
    private UserPreferences() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(HeartRateApplication.getContext());
        mEditor = mSharedPreferences.edit();
    }
    
    public static UserPreferences getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserPreferences();
        }
        return INSTANCE;
    }
}
