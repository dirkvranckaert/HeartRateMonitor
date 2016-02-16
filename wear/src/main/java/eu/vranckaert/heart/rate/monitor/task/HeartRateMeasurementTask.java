package eu.vranckaert.heart.rate.monitor.task;

import android.os.AsyncTask;
import android.util.Log;
import eu.vranckaert.heart.rate.monitor.WearBusinessService;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;

import java.util.List;

/**
 * Date: 03/06/15
 * Time: 12:41
 *
 * @author Dirk Vranckaert
 */
public class HeartRateMeasurementTask extends AsyncTask<List<Measurement>, Void, Void> {
    @Override
    protected void onPreExecute() {
        Log.d("dirk", "Start heart rate measurement sync task");
    }

    @Override
    protected Void doInBackground(List<Measurement>... params) {
        Log.d("dirk-background", "Heart rate measurement sync task is executing...");
        List<Measurement> measurements = params[0];
        WearBusinessService.getInstance().registerHeartRates(measurements);
        Log.d("dirk-background", "Heart rate measurement sync task is is done...");
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d("dirk", "Heart rate measurement sync task has executed");
    }
}
