package eu.vranckaert.heart.rate.monitor;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import eu.vranckaert.heart.rate.monitor.shared.dao.SetupDao;
import eu.vranckaert.heart.rate.monitor.shared.dao.HeartRateDatabaseHelper;

/**
 * Date: 28/05/15
 * Time: 09:29
 *
 * @author Dirk Vranckaert
 */
public class HeartRateApplication extends Application {
    private static HeartRateApplication INSTANCE;

    private static final Handler mHandler = new Handler();

    public HeartRateApplication() {
        INSTANCE = this;
    }

    public static HeartRateApplication getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HeartRateApplication();
        }

        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SetupDao.getInstance(HeartRateDatabaseHelper.class, getContext()).setup(HeartRateDatabaseHelper.DB_VERSION);
    }

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }

    public static final void runOnUiThread(Runnable runnable) {
        Log.d(HeartRateApplication.class.getSimpleName(), "runOnUiThread");
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            Log.d(HeartRateApplication.class.getSimpleName(), "runnable.run()");
            runnable.run();
        } else {
            Log.d(HeartRateApplication.class.getSimpleName(), "handler.post()");
            mHandler.post(runnable);
        }
    }
}
