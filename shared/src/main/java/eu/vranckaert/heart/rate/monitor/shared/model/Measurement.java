package eu.vranckaert.heart.rate.monitor.shared.model;

import android.content.Context;
import android.text.TextUtils;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import eu.vranckaert.heart.rate.monitor.shared.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Date: 29/05/15
 * Time: 17:45
 *
 * @author Dirk Vranckaert
 */
@DatabaseTable(tableName = Measurement.TABLE_NAME)
public class Measurement implements Serializable {
    public static final String TABLE_NAME = "MEASUREMENT";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_UNIQUE_KEY = "UNIQUE_KEY";
    public static final String COLUMN_AVERAGE = "AVERAGE";
    public static final String COLUMN_MIN = "MIN";
    public static final String COLUMN_MAX = "MAX";
    public static final String COLUMN_START = "START";
    public static final String COLUMN_FIRST_MEASUREMENT = "FIRST_MEASUREMENT";
    public static final String COLUMN_END = "END";
    public static final String COLUMN_MEASURED_VALUES = "MEASURED_VALUES";
    public static final String COLUMN_ACTIVITY = "ACTIVITY";
    public static final String COLUMN_SYNCED_WITH_GOOGLE_FIT = "GOOGLE_FIT";
    public static final String COLUMN_SYNCED_WITH_PHONE = "SYNCED_WITH_PHONE";

    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private Integer id;
    @DatabaseField(columnName = COLUMN_UNIQUE_KEY, unique = true)
    private String uniqueKey;
    @DatabaseField(columnName = COLUMN_AVERAGE)
    private float averageHeartBeat;
    @DatabaseField(columnName = COLUMN_MIN)
    private float minimumHeartBeat;
    @DatabaseField(columnName = COLUMN_MAX)
    private float maximumHeartBeat;
    @DatabaseField(columnName = COLUMN_START)
    private long startMeasurement;
    @DatabaseField(columnName = COLUMN_END)
    private long endMeasurement;
    @DatabaseField(columnName = COLUMN_FIRST_MEASUREMENT)
    private long firstMeasurement;
    private Map<Long, Float> measuredValues;
    @DatabaseField(columnName = COLUMN_MEASURED_VALUES)
    private String measuredValuesString;
    @DatabaseField(columnName = COLUMN_ACTIVITY)
    private int activity;
    @DatabaseField(columnName = COLUMN_SYNCED_WITH_GOOGLE_FIT)
    private boolean syncedWithGoogleFit;
    @DatabaseField(columnName = COLUMN_SYNCED_WITH_PHONE)
    private boolean syncedWithPhone;

    public static Measurement fromJSON(String json) {
        try {
            JSONObject jsonObject = null;
            jsonObject = new JSONObject(json);
            String uniqueKey = jsonObject.optString("uniqueKey");
            Double heartBeat = jsonObject.optDouble("averageHeartBeat");
            Double minHeartBeat = jsonObject.optDouble("minimumHeartBeat");
            Double maxHeartBeat = jsonObject.optDouble("maximumHeartBeat");
            Measurement measurement = new Measurement();
            measurement.uniqueKey = uniqueKey;
            measurement.setAverageHeartBeat(heartBeat.floatValue());
            measurement.setMinimumHeartBeat(minHeartBeat.floatValue());
            measurement.setMaximumHeartBeat(maxHeartBeat.floatValue());
            measurement.setStartMeasurement(jsonObject.optLong("startMeasurement"));
            measurement.setEndMeasurement(jsonObject.optLong("endMeasurement"));
            measurement.setFirstMeasurement(jsonObject.optLong("firstMeasurement"));
            measurement.setActivity(jsonObject.optInt("activity"));

            String measuredValuesString = jsonObject.optString("measuredValues");
            Map<Long, Float> measuredValues = measuredValuesFromJson(measuredValuesString);
            measurement.setMeasuredValues(measuredValues);

            return measurement;
        } catch (JSONException e) {
            return null;
        }
    }

    public static Map<Long, Float> measuredValuesFromJson(String json) {
        try {
            JSONArray measuredValuesArray = new JSONArray(json);
            Map<Long, Float> measuredValues = new HashMap<>();
            for (int i = 0; i < measuredValuesArray.length(); i++) {
                JSONObject measuredValue = new JSONObject(measuredValuesArray.optString(i));
                measuredValues
                        .put(measuredValue.optLong("key"), ((Double) measuredValue.optDouble("value")).floatValue());
            }
            return measuredValues;
        } catch (JSONException e) {
            return new HashMap<>();
        }
    }

    public static String toJSONList(List<Measurement> measurements) {
        JSONArray jsonArray = new JSONArray();
        for (Measurement measurement : measurements) {
            String json = measurement.toJSON();
            if (!TextUtils.isEmpty(json)) {
                jsonArray.put(json);
            }
        }
        return jsonArray.toString();
    }

    public static List<Measurement> fromJSONList(String json) {
        try {
            List<Measurement> measurements = new ArrayList<>();
            if (!TextUtils.isEmpty(json)) {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String jsonObject = jsonArray.getString(i);
                    Measurement measurement = Measurement.fromJSON(jsonObject.toString());
                    if (measurement != null) {
                        measurements.add(measurement);
                    }
                }
            }
            return measurements;
        } catch (JSONException e) {
            return new ArrayList<>();
        }
    }

    public static String getActivityName(Context context, int activity) {
        switch (activity) {
            case ActivityState.IN_VEHICLE:
                return context.getString(R.string.heart_rate_history_activity_vehicle);
            case ActivityState.WALKING:
                return context.getString(R.string.heart_rate_history_activity_walking);
            case ActivityState.ON_FOOT:
                return context.getString(R.string.heart_rate_history_activity_on_foot);
            case ActivityState.ON_BICYCLE:
                return context.getString(R.string.heart_rate_history_activity_bicycle);
            case ActivityState.RUNNING:
                return context.getString(R.string.heart_rate_history_activity_running);
            case ActivityState.STILL:
                return context.getString(R.string.heart_rate_history_activity_still);
            case ActivityState.TILTING:
                return context.getString(R.string.heart_rate_history_activity_tilting);
            default:
                return context.getString(R.string.heart_rate_history_activity_unknown);
        }
    }

    private static String generateUniqueKey() {
        return UUID.randomUUID().toString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void updateUniqueKey() {
        this.uniqueKey = generateUniqueKey();
    }

    public float getAverageHeartBeat() {
        return averageHeartBeat;
    }

    public void setAverageHeartBeat(float averageHeartBeat) {
        this.averageHeartBeat = averageHeartBeat;
    }

    public float getMinimumHeartBeat() {
        return minimumHeartBeat;
    }

    public void setMinimumHeartBeat(float minimumHeartBeat) {
        this.minimumHeartBeat = minimumHeartBeat;
    }

    public float getMaximumHeartBeat() {
        return maximumHeartBeat;
    }

    public void setMaximumHeartBeat(float maximumHeartBeat) {
        this.maximumHeartBeat = maximumHeartBeat;
    }

    public long getStartMeasurement() {
        return startMeasurement;
    }

    public void setStartMeasurement(long startMeasurement) {
        this.startMeasurement = startMeasurement;
    }

    public long getEndMeasurement() {
        return endMeasurement;
    }

    public void setEndMeasurement(long endMeasurement) {
        this.endMeasurement = endMeasurement;
    }

    public long getFirstMeasurement() {
        return firstMeasurement;
    }

    public void setFirstMeasurement(long firstMeasurement) {
        this.firstMeasurement = firstMeasurement;
    }

    public Map<Long, Float> getMeasuredValues() {
        measuredValues = measuredValuesFromJson(measuredValuesString);
        return measuredValues;
    }

    public void setMeasuredValues(Map<Long, Float> measuredValues) {
        this.measuredValues = measuredValues;
        measuredValuesString = measuredValuesToJSON();
    }

    public int getActivity() {
        return activity;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public boolean isSyncedWithGoogleFit() {
        return syncedWithGoogleFit;
    }

    public void setSyncedWithGoogleFit(boolean syncedWithGoogleFit) {
        this.syncedWithGoogleFit = syncedWithGoogleFit;
    }

    public boolean isSyncedWithPhone() {
        return syncedWithPhone;
    }

    public void setSyncedWithPhone(boolean syncedWithPhone) {
        this.syncedWithPhone = syncedWithPhone;
    }

    public String toJSON() {
        try {
            JSONObject json = new JSONObject();
            json.put("uniqueKey", getUniqueKey());
            json.put("averageHeartBeat", getAverageHeartBeat());
            json.put("minimumHeartBeat", getMinimumHeartBeat());
            json.put("maximumHeartBeat", getMaximumHeartBeat());
            json.put("startMeasurement", getStartMeasurement());
            json.put("endMeasurement", getEndMeasurement());
            json.put("firstMeasurement", getFirstMeasurement());
            json.put("activity", getActivity());
            getMeasuredValues(); // Make sure the measured values are filled correctly!
            json.put("measuredValues", measuredValuesToJSON());

            return json.toString();
        } catch (JSONException e) {
            return "";
        }
    }

    public String measuredValuesToJSON() {
        try {
            JSONArray measuredValuesArray = new JSONArray();
            for (Entry<Long, Float> entry : measuredValues.entrySet()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("key", entry.getKey());
                jsonObject.put("value", entry.getValue());
                measuredValuesArray.put(jsonObject.toString());
            }
            return measuredValuesArray.toString();
        } catch (JSONException e) {
            return "";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Measurement that = (Measurement) o;

        return !(uniqueKey != null ? !uniqueKey.equals(that.uniqueKey) : that.uniqueKey != null);

    }

    @Override
    public int hashCode() {
        return uniqueKey != null ? uniqueKey.hashCode() : 0;
    }

    public boolean isFakeHeartRate() {
        return !TextUtils.isEmpty(detectFakeHeartRate());
    }

    public String detectFakeHeartRate() {
        boolean timeCheckFailed = false;
        boolean measuredHeartRateCountFailed = false;
        boolean measurementHeartRateDropCheckFailed = false;
        boolean measuremntCannotAwakeFromDeathCheckFailed = false;

        // If it takes more than 60 seconds to find a heart beat it might be 'fake' one
        long timeBeforeFirstMeasurement = firstMeasurement - startMeasurement;
        if (timeBeforeFirstMeasurement > 60000) {
            timeCheckFailed = true;
        }

        // If we have 3 or less measurements it should be a fake heart rate
        int heartRateCount = getMeasuredValues() == null ? 0 : getMeasuredValues().size();
        if (heartRateCount <= 3) {
            measuredHeartRateCountFailed = true;
        }

        //
        if (getMeasuredValues() != null && !getMeasuredValues().isEmpty()) {
            TreeMap<Long, Float> sortedMap = new TreeMap<Long, Float>(new Comparator<Long>() {
                @Override
                public int compare(Long lhs, Long rhs) {
                    return lhs.compareTo(rhs);
                }
            });
            sortedMap.putAll(getMeasuredValues());

            long previousTimeStamp = -1;
            float previousHeartBeat = -1;
            for (Entry<Long, Float> entry : sortedMap.entrySet()) {
                long timeStamp = entry.getKey();
                float heartBeat = entry.getValue();
                if (previousHeartBeat != -1 && previousTimeStamp != -1) {
                    long timeBetween = timeStamp - previousTimeStamp;
                    if (heartBeat == 0 && previousHeartBeat > 100 && timeBetween <= 2000L) {
                        measurementHeartRateDropCheckFailed = true;
                    } else if (previousHeartBeat == 0 && heartBeat > 0) {
                        measuremntCannotAwakeFromDeathCheckFailed = true;
                    }
                }

                if (measurementHeartRateDropCheckFailed || measuremntCannotAwakeFromDeathCheckFailed) {
                    break;
                }

                previousTimeStamp = timeStamp;
                previousHeartBeat = heartBeat;
            }
        }

        String result = "";
        if (timeCheckFailed) {
            result = addSeperator(result, "|");
            result += "TIME_CHECK(" + (timeBeforeFirstMeasurement / 1000) + "s)";
        } else if (measuredHeartRateCountFailed) {
            result = addSeperator(result, "|");
            result += "HEART_RATE_COUNT(" + heartRateCount + ")";
        } else if (measurementHeartRateDropCheckFailed) {
            result = addSeperator(result, "|");
            result += "HEART_RATE_DROP";
        } else if (measuremntCannotAwakeFromDeathCheckFailed) {
            result = addSeperator(result, "|");
            result += "HEART_RATE_AWAKE_FROM_DEATH";
        }

        return result;
    }

    private String addSeperator(String result, String seperator) {
        if (!TextUtils.isEmpty(result)) {
            result += seperator;
        }
        return result;
    }

    public String getActivityName(Context context) {
        return getActivityName(context, activity);
    }
}
