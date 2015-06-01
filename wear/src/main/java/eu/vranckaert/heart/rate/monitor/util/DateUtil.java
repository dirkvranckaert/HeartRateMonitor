package eu.vranckaert.heart.rate.monitor.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Date: 31/03/15
 * Time: 17:02
 *
 * @author Dirk Vranckaert
 */
public class DateUtil {
    public static final String DAYS = "days";
    public static final String HOURS = "hours";
    public static final String MINUTES = "minutes";
    public static final String SECONDS = "seconds";
    public static final String MILLISECONDS = "milliseconds";

    public static final int DURATION_TYPE_DAYS = 0;
    public static final int DURATION_TYPE_HOURS = -1;
    public static final int DURATION_TYPE_MINUTES = -2;
    public static final int DURATION_TYPE_SECONDS = -3;
    public static final int DURATION_TYPE_MILLISECONDS = -4;

    private static SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private static SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static SimpleDateFormat sdf8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.getDefault());

    public static void resetDayInfo(Calendar calendar) {
        calendar.set(Calendar.YEAR, calendar.getActualMinimum(Calendar.YEAR));
        calendar.set(Calendar.MONTH, calendar.getActualMinimum(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMinimum(Calendar.DAY_OF_WEEK));
    }

    public static void resetTimeInfo(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        resetSecondsAndMillis(calendar);
    }

    public static void resetSecondsAndMillis(Calendar calendar) {
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
    }

    public static String formatTime(Date date) {
        return sdfTime.format(date);
    }

    public static String formatDateTime(Date date) {
        return sdfDateTime.format(date);
    }

    public static String formatDate(Date date) {
        return sdfDate.format(date);
    }

    public static Date parse8601Date(String date) {
        try {
            return sdf8601.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Calendar copyTimeIntoDate(Date time, Date date) {
        Calendar dateTime = Calendar.getInstance();
        dateTime.setTime(date);

        Calendar justTime = Calendar.getInstance();
        justTime.setTime(time);

        dateTime.set(Calendar.HOUR_OF_DAY, justTime.get(Calendar.HOUR_OF_DAY));
        dateTime.set(Calendar.MINUTE, justTime.get(Calendar.MINUTE));
        dateTime.set(Calendar.SECOND, justTime.get(Calendar.SECOND));
        dateTime.set(Calendar.MILLISECOND, justTime.get(Calendar.MILLISECOND));

        return dateTime;
    }

    public static Calendar calculateExpirationDate(long expiresIn) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, (int) expiresIn);
        return calendar;
    }

    public static boolean isToday(Date date) {
        Calendar calendar = Calendar.getInstance();
        resetTimeInfo(calendar);
        resetSecondsAndMillis(calendar);
        Date today = calendar.getTime();

        return (date.after(today) || date.getTime() == today.getTime());
    }

    public static int minutesBetween(Date dateStart, Date dateEnd) {
        int minutesBetween = 0;

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(dateStart);
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(dateEnd);
        while(calendarStart.before(calendarEnd)) {
            calendarStart.add(Calendar.MINUTE, 1);
            minutesBetween++;
        }
        return minutesBetween;
    }

    public static Map<String, Long> calculateDuration(long time, Integer... types) {
        Map<String, Long> duration = new HashMap<>();
        duration.put(DAYS, 0L);
        duration.put(HOURS, 0L);
        duration.put(MINUTES, 0L);
        duration.put(SECONDS, 0L);
        duration.put(MILLISECONDS, 0L);

        final long MILLIS_PER_SECOND = 1000;
        final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
        final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
        final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

        List<Integer> durationTypes = Arrays.asList(types);
        if (durationTypes.contains(DURATION_TYPE_DAYS)) {
            long days = time / MILLIS_PER_DAY;
            time -= days * MILLIS_PER_DAY;
            duration.put(DAYS, days);
        }

        if (durationTypes.contains(DURATION_TYPE_HOURS)) {
            long hours = time / MILLIS_PER_HOUR;
            time -= hours * MILLIS_PER_HOUR;
            duration.put(HOURS, hours);
        }

        if (durationTypes.contains(DURATION_TYPE_MINUTES)) {
            long minutes = time / MILLIS_PER_MINUTE;
            time -= minutes * MILLIS_PER_MINUTE;
            duration.put(MINUTES, minutes);
        }

        if (durationTypes.contains(DURATION_TYPE_SECONDS)) {
            long seconds = time / MILLIS_PER_SECOND;
            time -= seconds * MILLIS_PER_SECOND;
            duration.put(SECONDS, seconds);
        }

        if (durationTypes.contains(DURATION_TYPE_MILLISECONDS)) {
            long millis = time;
            duration.put(MILLISECONDS, millis);
        }

        return duration;
    }
}
