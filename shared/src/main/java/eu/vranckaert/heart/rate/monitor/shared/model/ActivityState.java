package eu.vranckaert.heart.rate.monitor.shared.model;

import eu.vranckaert.heart.rate.monitor.shared.util.DateUtil;

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
    public static final int DEFAULT_MEASURING_DURATION = 15000;
    public static final int DEFAULT_MEASURING_TIMEOUT = 120000;

    public static final int IN_VEHICLE = 0;
    public static final int ON_BICYCLE = 1;
    public static final int ON_FOOT = 2;
    public static final int STILL = 3;
    public static final int UNKNOWN = 4;
    public static final int TILTING = 5;
    public static final int WALKING = 7;
    public static final int RUNNING = 8;
}
