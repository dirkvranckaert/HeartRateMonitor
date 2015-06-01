package eu.vranckaert.heart.rate.monitor.controller;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

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

                startHearRateMonitor();
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
                calculateAverageHeartBeat();
            }
        }
    }

    private void calculateAverageHeartBeat() {
        Log.d("dirk-background", "calculateAverageHeartBeat");
        Log.d("dirk-background", "mMeasuredValues.size=" + mMeasuredValues.size());

        float sum = 0f;
        for (int i=0; i<mMeasuredValues.size(); i++) {
            float measuredValue = mMeasuredValues.get(i);
            sum += measuredValue;
        }
        float averageHearBeat = sum / mMeasuredValues.size();
        Log.d("dirk-background", "averageHearBeat=" + averageHearBeat);
        Toast.makeText(HeartRateMonitorIntentService.this, "averageHearBeat=" + averageHearBeat, Toast.LENGTH_LONG).show();
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
        Log.d("dirk-background", "event.sensor.getType() = " + event.sensor.getType());
        Log.d("dirk-background", "event.sensor.getStringType() = " + event.sensor.getStringType());
        Log.d("dirk-background", "event.sensor.getName() = " + event.sensor.getName());
        Log.d("dirk-background", "event.sensor.getVendor() = " + event.sensor.getVendor());
        Log.d("dirk-background", "event.values.length = " + event.values.length);
        for (int i = 0; i < event.values.length; i++) {
            float value = event.values[i];
            Log.d("dirk-background", "event.values[i] = " + value);
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
