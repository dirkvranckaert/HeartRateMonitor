package eu.vranckaert.heart.rate.monitor;

import android.app.Application;
import android.content.Context;

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

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }
}
