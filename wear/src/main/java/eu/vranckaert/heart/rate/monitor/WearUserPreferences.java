package eu.vranckaert.heart.rate.monitor;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import eu.vranckaert.heart.rate.monitor.shared.model.ActivityState;

/**
 * Date: 01/06/15
 * Time: 17:27
 *
 * @author Dirk Vranckaert
 */
public class WearUserPreferences {
    private static final String KEY_HAS_RUN_BEFORE = "has_run_before";
    private static final String KEY_PHONE_SETUP_COMPETED = "phone_setup_completed";
    private static final String KEY_HEART_RATE_MEASURING_INTERVAL = "heart_rate_measuring_interval";
    private static final String KEY_HEART_RATE_MEASURING_NOTIFICATION = "heart_rate_measuring_notification";
    private static final String KEY_HEART_RATE_AMBIENT_UI_UPDATE_TIME_LAPSE = "heart_rate_measuring_ui_update_time_lapse";
    private static final long HEART_RATE_AMBIENT_UI_UPDATE_TIME_LAPSE_DEFAULT = 10000L;

    private static WearUserPreferences INSTANCE;
    
    private final SharedPreferences mSharedPreferences;
    private final Editor mEditor;
    
    private WearUserPreferences() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(WearHeartRateApplication.getContext());
        mEditor = mSharedPreferences.edit();
    }
    
    public static WearUserPreferences getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WearUserPreferences();
        }
        return INSTANCE;
    }

    public void setHasRunBefore() {
        mEditor.putBoolean(KEY_HAS_RUN_BEFORE, true);
        mEditor.commit();
    }

    public void setHasntRunBefore() {
        mEditor.putBoolean(KEY_HAS_RUN_BEFORE, false);
        mEditor.commit();
    }

    public boolean hasRunBefore() {
        return mSharedPreferences.getBoolean(KEY_HAS_RUN_BEFORE, false);
    }

    public void setPhoneSetupCompleted(boolean setupCompleted) {
        mEditor.putBoolean(KEY_PHONE_SETUP_COMPETED, setupCompleted);
        mEditor.commit();
    }

    public boolean isPhoneSetupCompleted() {
        return mSharedPreferences.getBoolean(KEY_PHONE_SETUP_COMPETED, false);
    }

    public long getHeartRateMeasuringInterval() {
        String interval = mSharedPreferences.getString(KEY_HEART_RATE_MEASURING_INTERVAL, null);
        if (TextUtils.isEmpty(interval)) {
            return ActivityState.DEFAULT_MEASURING_INTERVAL;
        }
        return Long.valueOf(interval);
    }

    public boolean showHeartRateMeasuementNotification() {
        return mSharedPreferences.getBoolean(KEY_HEART_RATE_MEASURING_NOTIFICATION, false);
    }

    public long getAmbientModeUiUpdateTimeLapse() {
        String timeLapse = mSharedPreferences.getString(KEY_HEART_RATE_AMBIENT_UI_UPDATE_TIME_LAPSE, null);
        if (TextUtils.isEmpty(timeLapse)) {
            return HEART_RATE_AMBIENT_UI_UPDATE_TIME_LAPSE_DEFAULT;
        }
        return Long.valueOf(timeLapse);
    }
}
