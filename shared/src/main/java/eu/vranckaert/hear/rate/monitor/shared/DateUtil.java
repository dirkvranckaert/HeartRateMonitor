package eu.vranckaert.hear.rate.monitor.shared;

/**
 * Date: 02/06/15
 * Time: 08:18
 *
 * @author Dirk Vranckaert
 */
public class DateUtil {
    public static long convertMinutesToMillis(int minutes) {
        return minutes * 60L * 1000L;
    }
}
