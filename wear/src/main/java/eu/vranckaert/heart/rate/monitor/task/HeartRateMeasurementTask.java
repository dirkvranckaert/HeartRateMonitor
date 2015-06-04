package eu.vranckaert.heart.rate.monitor.task;

import android.os.AsyncTask;
import android.util.Log;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.WearBusinessService;

/**
 * Date: 03/06/15
 * Time: 12:41
 *
 * @author Dirk Vranckaert
 */
public class HeartRateMeasurementTask extends AsyncTask<Measurement, Void, Void> {
    @Override
    protected void onPreExecute() {
        Log.d("dirk", "Start heart rate measurement sync task");
    }

    @Override
    protected Void doInBackground(Measurement... params) {
        Log.d("dirk-background", "Heart rate measurement sync task is executing...");
        Measurement measurements = params[0];
        WearBusinessService.getInstance().registerHeartRate(measurements);
        Log.d("dirk-background", "Heart rate measurement sync task is is done...");
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d("dirk", "Heart rate measurement sync task has executed");
    }
}
