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
    private long startMeasurement;
    private long endMeasurement;
    private int activity;

    public float getAverageHeartBeat() {
        return averageHeartBeat;
    }

    public void setAverageHeartBeat(float averageHeartBeat) {
        this.averageHeartBeat = averageHeartBeat;
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
            Measurement measurement = new Measurement();
            measurement.setAverageHeartBeat(heartBeat.floatValue());
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
}
