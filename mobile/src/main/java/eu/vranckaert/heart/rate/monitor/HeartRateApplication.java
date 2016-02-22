package eu.vranckaert.heart.rate.monitor;

import android.content.Context;
import eu.vranckaert.heart.rate.monitor.shared.AbstractHeartRateApplication;
import eu.vranckaert.heart.rate.monitor.shared.dao.HeartRateDatabaseHelper;
import eu.vranckaert.heart.rate.monitor.shared.dao.SetupDao;

/**
 * Date: 28/05/15
 * Time: 09:29
 *
 * @author Dirk Vranckaert
 */
public class HeartRateApplication extends AbstractHeartRateApplication {
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

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SetupDao.getInstance(HeartRateDatabaseHelper.class, getContext()).setup(HeartRateDatabaseHelper.DB_VERSION);
    }
}
