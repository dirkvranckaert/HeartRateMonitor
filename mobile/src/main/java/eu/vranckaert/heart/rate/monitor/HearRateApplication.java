package eu.vranckaert.heart.rate.monitor;

import android.app.Application;
import android.content.Context;

/**
 * Date: 28/05/15
 * Time: 09:29
 *
 * @author Dirk Vranckaert
 */
public class HearRateApplication extends Application {
    private static HearRateApplication INSTANCE;

    public HearRateApplication() {
        INSTANCE = this;
    }

    public static HearRateApplication getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HearRateApplication();
        }

        return INSTANCE;
    }

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }
}
