package eu.vranckaert.heart.rate.monitor.task;

import android.os.AsyncTask;
import android.util.Log;
import eu.vranckaert.heart.rate.monitor.BusinessService;

/**
 * Date: 01/06/15
 * Time: 08:19
 *
 * @author Dirk Vranckaert
 */
public class ActivityRecognitionTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... params) {
        Log.d("dirk", "About to setup activity recognition");
        //BusinessService.getInstance().connectActivityRecognitionApiClient();
        //Log.d("dirk", "Activity recognition setup completed");
        Log.d("dirk", "Activity recognition not stated, disabled hard coded for now");
        return null;
    }
}
