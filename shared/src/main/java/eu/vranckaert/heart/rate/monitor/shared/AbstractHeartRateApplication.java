package eu.vranckaert.heart.rate.monitor.shared;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Date: 22/02/16
 * Time: 22:17
 *
 * @author Dirk Vranckaert
 */
public class AbstractHeartRateApplication extends Application {
    private static final Handler mHandler = new Handler();

    public static final void runOnUiThread(Runnable runnable) {
        Log.d(AbstractHeartRateApplication.class.getSimpleName(), "runOnUiThread");
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            Log.d(AbstractHeartRateApplication.class.getSimpleName(), "runnable.run()");
            runnable.run();
        } else {
            Log.d(AbstractHeartRateApplication.class.getSimpleName(), "handler.post()");
            mHandler.post(runnable);
        }
    }
}
