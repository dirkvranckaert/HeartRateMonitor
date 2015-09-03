package eu.vranckaert.hear.rate.monitor.shared.model;

import eu.vranckaert.hear.rate.monitor.shared.util.DateUtil;

/**
 * Date: 01/06/15
 * Time: 17:18
 *
 * @author Dirk Vranckaert
 */
public class ActivityState {
    /*
     * With current settings activity detection will receive updates every 30 seconds.
     * If a new activity is received, it's only trusted after it's been seen for 3 minutes (calculated with the trusted factor).
     * Automatic measuring will, by default, run every 60 minutes (once ever hour). If we detect that you are doing some sports
     * (like running or bycycling) the measuring interval is set to once every five minutes. If we detect that you are in
     * a vehicle (your car, the bus, the train,...) we measure every 15 minutes.
     */
    public static final long DEFAULT_MEASURING_INTERVAL = DateUtil.convertMinutesToMillis(60);
    public static final long DETECTION_INTERVAL = 30000; // 30 seconds
    public static final int TRUSTED_FACTOR = 6; // TRUSTED_FACTOR * DETECTION_INTERVAL = time before an activity is accepted. So 6 * 30000 = 180000 millis or 180 seconds or 3 minutes before we accept that a user is running/bicycling/driving/still/...

    public static final int IN_VEHICLE = 0;
    public static final int ON_BICYCLE = 1;
    public static final int ON_FOOT = 2;
    public static final int STILL = 3;
    public static final int UNKNOWN = 4;
    public static final int TILTING = 5;
    public static final int WALKING = 7;
    public static final int RUNNING = 8;

    public static long getMeasuringIntervalForActivity(int activity) {
        long interval = DEFAULT_MEASURING_INTERVAL;

        // For now disabling the activity detection, should be configurable in the application
        if (false) {
            switch (activity) {
                case ActivityState.IN_VEHICLE:
                    interval = DateUtil.convertMinutesToMillis(15);
                    break;
                case ActivityState.ON_BICYCLE:
                    interval = DateUtil.convertMinutesToMillis(5);
                    break;
                case ActivityState.RUNNING:
                    interval = DateUtil.convertMinutesToMillis(5);
                    break;
            }
        }

        return interval;
    }
}
