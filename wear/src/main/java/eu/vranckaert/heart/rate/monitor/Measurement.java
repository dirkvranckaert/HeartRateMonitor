package eu.vranckaert.heart.rate.monitor;

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

    public String toJSON() throws JSONException {
        JSONObject json = new JSONObject("measurement");
        json.put("averageHeartBeat", averageHeartBeat);
        json.put("startMeasurement", startMeasurement);
        json.put("endMeasurement", endMeasurement);
        return json.toString();
    }

    public static Measurement fromJSON(JSONObject jsonObject) {
        Double heartBeat = jsonObject.optDouble("averageHeartBeat");
        Measurement measurement = new Measurement();
        measurement.setAverageHeartBeat(heartBeat.floatValue());
        measurement.setStartMeasurement(jsonObject.optLong("startMeasurement"));
        measurement.setEndMeasurement(jsonObject.optLong("endMeasurement"));
        return measurement;
    }

    public static JSONArray toJSONList(List<Measurement> measurements) throws JSONException {
        JSONArray jsonArray = new JSONArray("measurements");
        for (Measurement measurement : measurements) {
            jsonArray.put(measurement.toJSON());
        }
        return jsonArray;
    }

    public static List<Measurement> fromJSONList(String json) throws JSONException {
        List<Measurement> measurements = new ArrayList<>();
        if (!TextUtils.isEmpty(json)) {
            JSONArray jsonArray = new JSONArray(json);
            for (int i=0; i<jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Measurement measurement = Measurement.fromJSON(jsonObject);
                measurements.add(measurement);
            }
        }
        return measurements;
    }
}
