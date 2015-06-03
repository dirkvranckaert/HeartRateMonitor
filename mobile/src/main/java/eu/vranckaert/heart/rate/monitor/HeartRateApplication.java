package eu.vranckaert.heart.rate.monitor;

import android.app.Application;
import android.content.Context;

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

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }
}
