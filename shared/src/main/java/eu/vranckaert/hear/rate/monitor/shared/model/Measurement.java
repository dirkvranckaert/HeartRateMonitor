package eu.vranckaert.hear.rate.monitor.shared.model;

import android.text.TextUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 29/05/15
 * Time: 17:45
 *
 * @author Dirk Vranckaert
 */
public class Measurement implements Serializable {
    private float averageHeartBeat;
    private float minimumHeartBeat;
    private float maximumHeartBeat;
    private long startMeasurement;
    private long endMeasurement;
    private int activity;

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

    public int getActivity() {
        return activity;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public String toJSON() {
        try {
            JSONObject json = new JSONObject();
            json.put("averageHeartBeat", getAverageHeartBeat());
            json.put("minimumHeartBeat", getMinimumHeartBeat());
            json.put("maximumHeartBeat", getMaximumHeartBeat());
            json.put("startMeasurement", getStartMeasurement());
            json.put("endMeasurement", getEndMeasurement());
            json.put("activity", getActivity());
            return json.toString();
        } catch (JSONException e) {
            return "";
        }
    }

    public static Measurement fromJSON(String json) {
        try {
            JSONObject jsonObject = null;
            jsonObject = new JSONObject(json);
            Double heartBeat = jsonObject.optDouble("averageHeartBeat");
            Double minHeartBeat = jsonObject.optDouble("minimumHeartBeat");
            Double maxHeartBeat = jsonObject.optDouble("maximumHeartBeat");
            Measurement measurement = new Measurement();
            measurement.setAverageHeartBeat(heartBeat.floatValue());
            measurement.setMinimumHeartBeat(minHeartBeat.floatValue());
            measurement.setMaximumHeartBeat(maxHeartBeat.floatValue());
            measurement.setStartMeasurement(jsonObject.optLong("startMeasurement"));
            measurement.setEndMeasurement(jsonObject.optLong("endMeasurement"));
            measurement.setActivity(jsonObject.optInt("activity"));
            return measurement;
        } catch (JSONException e) {
            return null;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Measurement that = (Measurement) o;

        if (Float.compare(that.averageHeartBeat, averageHeartBeat) != 0) {
            return false;
        }
        if (startMeasurement != that.startMeasurement) {
            return false;
        }
        return endMeasurement == that.endMeasurement;

    }

    @Override
    public int hashCode() {
        int result = (averageHeartBeat != +0.0f ? Float.floatToIntBits(averageHeartBeat) : 0);
        result = 31 * result + (int) (startMeasurement ^ (startMeasurement >>> 32));
        result = 31 * result + (int) (endMeasurement ^ (endMeasurement >>> 32));
        return result;
    }
}
