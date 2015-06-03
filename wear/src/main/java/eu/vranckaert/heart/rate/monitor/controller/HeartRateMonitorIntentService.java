package eu.vranckaert.heart.rate.monitor.controller;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.util.Log;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.WearUserPreferences;
import eu.vranckaert.heart.rate.monitor.util.DeviceUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Date: 28/05/15
 * Time: 09:33
 *
 * @author Dirk Vranckaert
 */
public class HeartRateMonitorIntentService extends IntentService implements SensorEventListener {
    private long mStarTime = -1;
    private long mEndTime = -1;

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private boolean mMeasuring;
    private List<Float> mMeasuredValues = new ArrayList<>();

    public HeartRateMonitorIntentService() {
        super(HeartRateMonitorIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("dirk-background", "onHandleIntent");

        if (getSystemService(Context.SENSOR_SERVICE) != null) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null) {
                mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
                if (!DeviceUtil.isCharging()) {
                    startHearRateMonitor();
                } else {
                    Log.d("dirk-background", "Will not start heart rate monitoring as device is currently charging");
                }
            }
        }
    }

    private void checkDuration() {
        if (mStarTime == -1 || mEndTime == -1) {
            Log.d("dirk-background", "checkDuration (set initial values)");
            // Determine the end time of the measurement (aka the duration)
            Calendar calendar = Calendar.getInstance();
            mStarTime = calendar.getTimeInMillis();
            calendar.add(Calendar.MILLISECOND, 15000);
            mEndTime = calendar.getTimeInMillis();
        } else {
            Log.d("dirk-background", "checkDuration");
            long currentTime = new Date().getTime();
            if (currentTime >= mEndTime) {
                stopHearRateMonitor();
                float heartBeat = calculateAverageHeartBeat();
                Measurement measurement = new Measurement();
                measurement.setAverageHeartBeat(heartBeat);
                measurement.setStartMeasurement(mStarTime);
                measurement.setEndMeasurement(currentTime);
                WearUserPreferences.getInstance().addMeasurement(measurement);

                // TODO notify UI to be updated if visible right now...
            }
        }
    }

    private float calculateAverageHeartBeat() {
        Log.d("dirk-background", "calculateAverageHeartBeat");
        Log.d("dirk-background", "mMeasuredValues.size=" + mMeasuredValues.size());

        float sum = 0f;
        for (int i = 0; i < mMeasuredValues.size(); i++) {
            float measuredValue = mMeasuredValues.get(i);
            sum += measuredValue;
        }
        float averageHearBeat = sum / mMeasuredValues.size();
        Log.d("dirk-background", "averageHearBeat=" + averageHearBeat);
        return averageHearBeat;
    }

    private void startHearRateMonitor() {
        Log.d("dirk-background", "startHearRateMonitor");
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_UI);
    }

    private void stopHearRateMonitor() {
        Log.d("dirk-background", "stopHearRateMonitor");
        mMeasuring = false;
        mSensorManager.unregisterListener(this, mHeartRateSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("dirk-background", "onSensorChanged");
        for (int i = 0; i < event.values.length; i++) {
            float value = event.values[i];
            Log.d("dirk-background", "event.values[" + i + "] = " + value);
        }

        if (event.values.length > 0 && (event.values[event.values.length - 1] > 0 || mMeasuring)) {
            mMeasuring = true;
            float value = event.values[event.values.length - 1];
            mMeasuredValues.add(value);
            checkDuration();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
