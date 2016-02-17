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
public class HeartRateMeasurementTask extends AsyncTask<List<Measurement>, Void, Boolean> {
    private final HeartRateMeasurementTaskListener mListener;

    public HeartRateMeasurementTask() {
        this(null);
    }

    public HeartRateMeasurementTask(HeartRateMeasurementTaskListener listener) {
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        Log.d("dirk", "Start heart rate measurement sync task");
        if (mListener != null) {
            mListener.beforeSync();
        }
    }

    @Override
    protected Boolean doInBackground(List<Measurement>... params) {
        Log.d("dirk-background", "Heart rate measurement sync task is executing...");
        List<Measurement> measurements = params[0];
        boolean result = WearBusinessService.getInstance().registerHeartRates(measurements);
        Log.d("dirk-background", "Heart rate measurement sync task is is done...");
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Log.d("dirk", "Heart rate measurement sync task has executed (success?" + result + ")");
        if (mListener != null) {
            mListener.afterSync(result);
        }
    }

    public interface HeartRateMeasurementTaskListener {
        void beforeSync();
        void afterSync(boolean success);
    }
}
