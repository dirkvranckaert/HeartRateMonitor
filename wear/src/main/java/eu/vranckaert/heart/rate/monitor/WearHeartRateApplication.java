package eu.vranckaert.heart.rate.monitor;

import android.app.Application;
import android.content.Context;
import eu.vranckaert.heart.rate.monitor.shared.dao.HeartRateDatabaseHelper;
import eu.vranckaert.heart.rate.monitor.shared.dao.SetupDao;

/**
 * Date: 28/05/15
 * Time: 09:29
 *
 * @author Dirk Vranckaert
 */
public class WearHeartRateApplication extends Application {
    private static WearHeartRateApplication INSTANCE;

    public WearHeartRateApplication() {
        INSTANCE = this;
    }

    public static WearHeartRateApplication getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WearHeartRateApplication();
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
