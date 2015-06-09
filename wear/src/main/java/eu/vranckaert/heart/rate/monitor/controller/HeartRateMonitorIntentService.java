package eu.vranckaert.heart.rate.monitor.controller;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.WearUserPreferences;
import eu.vranckaert.heart.rate.monitor.task.HeartRateMeasurementTask;
import eu.vranckaert.heart.rate.monitor.util.DeviceUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Date: 28/05/15
 * Time: 09:33
 *
 * @author Dirk Vranckaert
 */
public class HeartRateMonitorIntentService extends IntentService implements SensorEventListener {
    private long mStartTime = -1;
    private long mFirstMeasurement = -1;
    private long mEndTime = -1;

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private boolean mMeasuring;
    private Map<Long, Float> mMeasuredValues = new HashMap<>();
    private float mMaximumHeartBeat = -1;
    private float mMinimumHeartBeat = -1;

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
        if (mStartTime == -1 || mEndTime == -1) {
            Log.d("dirk-background", "checkDuration (set initial values)");
            // Determine the end time of the measurement (aka the duration)
            Calendar calendar = Calendar.getInstance();
            mStartTime = calendar.getTimeInMillis();
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
                measurement.setMinimumHeartBeat(mMinimumHeartBeat);
                measurement.setMaximumHeartBeat(mMaximumHeartBeat);
                measurement.setStartMeasurement(mStartTime);
                measurement.setEndMeasurement(currentTime);
                measurement.setFirstMeasurement(mFirstMeasurement);
                WearUserPreferences.getInstance().addMeasurement(measurement);
                new HeartRateMeasurementTask().execute(measurement);

                // TODO notify UI to be updated if visible right now...
            }
        }
    }

    private float calculateAverageHeartBeat() {
        Log.d("dirk-background", "calculateAverageHeartBeat");
        Log.d("dirk-background", "mMeasuredValues.size=" + mMeasuredValues.size());

        float sum = 0f;
        for (Entry<Long, Float> entry : mMeasuredValues.entrySet()) {
            float measuredValue = entry.getValue();
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
            long currentTime = new Date().getTime();
            mMeasuring = true;
            if (mFirstMeasurement == -1) {
                mFirstMeasurement = currentTime;
            }
            float value = event.values[event.values.length - 1];
            mMeasuredValues.put(currentTime, value);
            if (mMinimumHeartBeat == -1 || value < mMinimumHeartBeat) {
                mMinimumHeartBeat = value;
            }
            if (mMaximumHeartBeat == -1 || value > mMaximumHeartBeat) {
                mMaximumHeartBeat = value;
            }
            checkDuration();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
