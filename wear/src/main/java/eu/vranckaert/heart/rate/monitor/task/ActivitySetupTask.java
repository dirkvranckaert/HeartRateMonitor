package eu.vranckaert.heart.rate.monitor.task;

import android.os.AsyncTask;
import eu.vranckaert.heart.rate.monitor.WearBusinessService;
import eu.vranckaert.heart.rate.monitor.WearUserPreferences;

/**
 * Date: 01/06/15
 * Time: 18:06
 *
 * @author Dirk Vranckaert
 */
public class ActivitySetupTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... params) {
        if (WearUserPreferences.getInstance().isPhoneSetupCompleted()) {
            WearBusinessService.getInstance().requestActivityUpdates();
        }
        return null;
    }
}
