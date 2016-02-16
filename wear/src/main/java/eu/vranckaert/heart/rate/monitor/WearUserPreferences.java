package eu.vranckaert.heart.rate.monitor;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import eu.vranckaert.heart.rate.monitor.shared.model.ActivityState;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 01/06/15
 * Time: 17:27
 *
 * @author Dirk Vranckaert
 */
public class WearUserPreferences {
    private static final String KEY_HAS_RUN_BEFORE = "has_run_before";
    private static final String KEY_LATEST_ACTIVITY = "latest_activity";
    private static final String KEY_LATEST_ACTIVITY_COUNT = "latest_activity_count";
    private static final String KEY_ACCEPTED_ACTIVITY = "accepted_activity";
    private static final String KEY_ALL_MEASUREMENTS = "all_measurements";
    private static final String KEY_LATEST_MEASUREMENTS = "latest_measurements";
    private static final String KEY_PHONE_SETUP_COMPETED = "phone_setup_completed";

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

    public void storeLatestActivity(int activityState) {
        int previousActivity = getLatestActivity();
        int count;
        if (previousActivity == activityState) {
            count = getLatestActivityCount() + 1;
        } else {
            count = 1;
        }
        Log.d("dirk", "Storing activity (" + activityState + ") and count (" + count + ")");
        mEditor.putInt(KEY_LATEST_ACTIVITY_COUNT, count);
        mEditor.putInt(KEY_LATEST_ACTIVITY, activityState);
        mEditor.commit();
    }

    public int getLatestActivity() {
        return mSharedPreferences.getInt(KEY_LATEST_ACTIVITY, ActivityState.STILL);
    }

    public int getLatestActivityCount() {
        return mSharedPreferences.getInt(KEY_LATEST_ACTIVITY_COUNT, 0);
    }

    public void setAcceptedActivity(int activityState) {
        mEditor.putInt(KEY_ACCEPTED_ACTIVITY, activityState);
        mEditor.commit();
    }

    public int getAcceptedActivity() {
        return mSharedPreferences.getInt(KEY_ACCEPTED_ACTIVITY, ActivityState.STILL);
    }

    public void setPhoneSetupCompleted(boolean setupCompleted) {
        mEditor.putBoolean(KEY_PHONE_SETUP_COMPETED, setupCompleted);
        mEditor.commit();
    }

    public boolean isPhoneSetupCompleted() {
        return mSharedPreferences.getBoolean(KEY_PHONE_SETUP_COMPETED, false);
    }
}
