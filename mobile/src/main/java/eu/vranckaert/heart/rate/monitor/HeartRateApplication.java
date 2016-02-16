package eu.vranckaert.heart.rate.monitor;

import android.app.Application;
import android.content.Context;
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
}
