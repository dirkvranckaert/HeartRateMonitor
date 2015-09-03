package eu.vranckaert.hear.rate.monitor.shared.util;

import android.content.Context;

/**
 * Date: 26/03/14
 * Time: 07:35
 *
 * @author Dirk Vranckaert
 */
public class MetricHelper {

    public static int dpToPixel(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int)(dp * density);
    }

    public static int pixelToDp(Context context, int pixel) {
        return (int) (context.getResources().getDisplayMetrics().density * pixel);
    }
}

