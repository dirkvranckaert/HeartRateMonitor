package eu.vranckaert.heart.rate.monitor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Date: 28/05/15
 * Time: 08:03
 *
 * @author Dirk Vranckaert
 */
public class HearRateActivity extends WearableActivity implements SensorEventListener, OnClickListener {
    private TextView mHearRate;
    private TextView mDiagnostics;
    private Button mStartStop;

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private boolean mMeasuring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpContentView();

        setAmbientEnabled();
    }



    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        Log.d("dirk", "onEnterAmbient");
        Log.d("dirk", "isAmbient=" + isAmbient());
        setUpContentView();

        super.onEnterAmbient(ambientDetails);
    }

    @Override
    public void onExitAmbient() {
        Log.d("dirk", "onExitAmbient");
        Log.d("dirk", "isAmbient=" + isAmbient());
        setUpContentView();

        super.onExitAmbient();
    }

    private void setUpContentView() {
        setContentView(isAmbient() ? R.layout.heart_rate_ambient : R.layout.heart_rate);
        mHearRate = (TextView) findViewById(R.id.heart_rate);
        mDiagnostics = (TextView) findViewById(R.id.diagnostics);
        mStartStop = (Button) findViewById(R.id.start_stop);
        if (mStartStop != null) {
            mStartStop.setOnClickListener(this);
            if (mMeasuring) {
                mStartStop.setText("STOP");
            } else {
                mStartStop.setText("START");
            }
        }
        if (findViewById(R.id.schedule_test) != null) {
            findViewById(R.id.schedule_test).setOnClickListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("dirk", "onSensorChanged");
        Log.d("dirk", "event.sensor.getType() = " + event.sensor.getType());
        Log.d("dirk", "event.sensor.getStringType() = " + event.sensor.getStringType());
        Log.d("dirk", "event.sensor.getName() = " + event.sensor.getName());
        Log.d("dirk", "event.sensor.getVendor() = " + event.sensor.getVendor());
        Log.d("dirk", "event.values.length = " + event.values.length);
        for (int i = 0; i < event.values.length; i++) {
            float value = event.values[i];
            Log.d("dirk", "event.values[i] = " + value);
        }

        mDiagnostics.setText("event.values.length=" + event.values.length);
        if (event.values.length > 0) {
            mHearRate.setText(event.values[event.values.length - 1] + " BPM");
        } else {
            mHearRate.setText("Searching for your heart rate");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("dirk", "onAccuracyChanged:" + accuracy);
    }

    @Override
    protected void onDestroy() {
        stopHearRateMonitor();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_stop) {
            if (!mMeasuring) {
                startHearRateMonitor();
            } else {
                stopHearRateMonitor();
            }
        } else if (v.getId() == R.id.schedule_test) {
            AlarmSchedulingService.getInstance().scheduleHeartRateMonitorInXMillis(30000);
        }
    }

    private void startHearRateMonitor() {
        if (mStartStop != null) {
            mStartStop.setText("STOP");
        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mMeasuring = true;
    }

    private void stopHearRateMonitor() {
        mHearRate.setText("-- BPM");
        mDiagnostics.setText(null);
        if (mStartStop != null) {
            mStartStop.setText("START");
        }
        mMeasuring = false;
        if (mHeartRateSensor != null) {
            mSensorManager.unregisterListener(this, mHeartRateSensor);
        }
    }
}
