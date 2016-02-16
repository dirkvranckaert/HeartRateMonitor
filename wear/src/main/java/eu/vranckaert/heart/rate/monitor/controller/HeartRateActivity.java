package eu.vranckaert.heart.rate.monitor.controller;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import eu.vranckaert.heart.rate.monitor.shared.permission.PermissionUtil;
import eu.vranckaert.heart.rate.monitor.WearUserPreferences;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.task.ActivitySetupTask;
import eu.vranckaert.heart.rate.monitor.task.HeartRateMeasurementTask;
import eu.vranckaert.heart.rate.monitor.util.DeviceUtil;
import eu.vranckaert.heart.rate.monitor.view.AbstractViewHolder;
import eu.vranckaert.heart.rate.monitor.view.HeartRateHistoryView;
import eu.vranckaert.heart.rate.monitor.view.HeartRateMonitorView;
import eu.vranckaert.heart.rate.monitor.view.HeartRateSetupView;
import eu.vranckaert.heart.rate.monitor.view.HeartRateUnavailableView;
import eu.vranckaert.heart.rate.monitor.view.HeartRateView;
import eu.vranckaert.heart.rate.monitor.view.HeartRateView.HeartRateListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Date: 28/05/15
 * Time: 08:03
 *
 * @author Dirk Vranckaert
 */
public class HeartRateActivity extends WearableActivity implements SensorEventListener, HeartRateListener {
    private static final int REQUEST_CODE_PERMISSION_BODY_SENSOR = 0;

    private HeartRateView mView;

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private boolean mMeasuring;
    private long mStartTimeMeasurement;
    private boolean mFirstValueFound;
    private Map<Long, Float> mMeasuredValues;
    private long mFirstMeasurement = -1;
    private float mMaximumHeartBeat = -1;
    private float mMinimumHeartBeat = -1;

    private HeartRateMonitorView mMonitorView;
    private HeartRateHistoryView mHistoryView;
    private boolean mInputLocked;

    // TODO start using the maximum heart rate (MHR): http://www.calculatenow.biz/sport/heart.php?age=28&submit=Calculate+MHR#mhr
    // This is based on the users age (below 30 or above 30). If average measured heart beat is significantly higher than
    // the MHR we could notify the user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSystemService(Context.SENSOR_SERVICE) != null) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null) {
                mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

                boolean phoneSetupCompleted = WearUserPreferences.getInstance().isPhoneSetupCompleted();
                if (phoneSetupCompleted) {
                    setAmbientEnabled();
                    if (mView == null) {
                        mView = new HeartRateView(this, this);
                    }
                    setContentView(mView.getView());

                    if (!WearUserPreferences.getInstance().hasRunBefore()) {
                        WearUserPreferences.getInstance().setHasRunBefore();
                        new ActivitySetupTask().execute();
                        SetupBroadcastReceiver.setupMeasuring();
                    }
                } else {
                    phoneSetupNotYetCompleted();
                }
            } else {
                heartRateSensorNotSupported();
            }
        } else {
            heartRateSensorNotSupported();
        }
    }

    private void heartRateSensorNotSupported() {
        setContentView(new HeartRateUnavailableView(this).getView());
    }

    private void phoneSetupNotYetCompleted() {
        setContentView(new HeartRateSetupView(this).getView());
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        Log.d("dirk", "onEnterAmbient");
        Log.d("dirk", "isAmbient=" + isAmbient());
        if (mMeasuring) {
            // TODO notify views to enter ambient mode
        } else {
            finish();
        }

        super.onEnterAmbient(ambientDetails);
    }

    @Override
    public void onExitAmbient() {
        Log.d("dirk", "onExitAmbient");
        Log.d("dirk", "isAmbient=" + isAmbient());
        // TODO notify views to exit ambient mode

        super.onExitAmbient();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("dirk", "onSensorChanged");
        for (int i = 0; i < event.values.length; i++) {
            float value = event.values[i];
            Log.d("dirk", "event.values[i] = " + value);
        }

        if (DeviceUtil.isCharging()) {
            // TODO show message to the user somehow that while charging the heart rate cannot be measured...
            clearMeasuredValues();
            mMinimumHeartBeat = -1;
            mMaximumHeartBeat = -1;
            stopHearRateMonitor();
            loadHistoricalData();
            return;
        }
        if (event.values.length > 0) {
            float value = event.values[event.values.length - 1];
            if (mFirstValueFound || value > 0f) {
                long currentTime = new Date().getTime();
                mFirstValueFound = true;
                if (mFirstMeasurement == -1) {
                    mFirstMeasurement = currentTime;
                }
                addMeasuredValue(currentTime, value);
                if (mMinimumHeartBeat == -1 || value < mMinimumHeartBeat) {
                    mMinimumHeartBeat = value;
                }
                if (mMaximumHeartBeat == -1 || value > mMaximumHeartBeat) {
                    mMaximumHeartBeat = value;
                }
                mMonitorView.setMeasuringHeartBeat((int) value);
            }
        }

    }

    private void addMeasuredValue(long currentTime, float value) {
        if (mMeasuredValues == null) {
            clearMeasuredValues();
        }
        mMeasuredValues.put(currentTime, value);
    }

    private void clearMeasuredValues() {
        mMeasuredValues = new HashMap<>();
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

    private void startHearRateMonitor() {
        mFirstValueFound = false;
        clearMeasuredValues();
        mMinimumHeartBeat = -1;
        mMaximumHeartBeat = -1;
        if (mSensorManager != null && mHeartRateSensor != null) {
            mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
            mMeasuring = true;
            mStartTimeMeasurement = new Date().getTime();
        }
    }

    private void stopHearRateMonitor() {
        mMeasuring = false;
        if (mSensorManager != null && mHeartRateSensor != null) {
            mSensorManager.unregisterListener(this, mHeartRateSensor);
        }

        if (mMeasuredValues != null && !mMeasuredValues.isEmpty()) {
            final float averageHeartBeat = calculateAverageHeartBeat();
            Measurement measurement = new Measurement();
            measurement.setAverageHeartBeat(averageHeartBeat);
            measurement.setMinimumHeartBeat(mMinimumHeartBeat);
            measurement.setMaximumHeartBeat(mMaximumHeartBeat);
            measurement.setStartMeasurement(mStartTimeMeasurement);
            measurement.setEndMeasurement(new Date().getTime());
            measurement.setFirstMeasurement(mFirstMeasurement);
            measurement.setMeasuredValues(mMeasuredValues);
            WearUserPreferences.getInstance().addMeasurement(measurement);
            new HeartRateMeasurementTask().execute(measurement);
            mMeasuredValues = null;
        }
    }

    private float calculateAverageHeartBeat() {
        Log.d("dirk", "calculateAverageHeartBeat");
        Log.d("dirk", "mMeasuredValues.size=" + mMeasuredValues.size());

        float sum = 0f;
        for (Entry<Long, Float> entry : mMeasuredValues.entrySet()) {
            float measuredValue = entry.getValue();
            sum += measuredValue;
        }

        float averageHearBeat = sum / mMeasuredValues.size();
        Log.d("dirk", "averageHearBeat=" + averageHearBeat);
        return averageHearBeat;
    }

    @Override
    public void onHearRateViewCreated(AbstractViewHolder view) {
        if (view instanceof HeartRateMonitorView) {
            mMonitorView = (HeartRateMonitorView) view;
        } else if (view instanceof HeartRateHistoryView) {
            mHistoryView = (HeartRateHistoryView) view;
        }

        if (mMonitorView != null && mHistoryView != null) {
            loadHistoricalData();
        }
    }

    private void loadHistoricalData() {
        Measurement latestMeasurement = WearUserPreferences.getInstance().getLatestMeasurment();
        mMonitorView.setLatestMeasurement(latestMeasurement);
        mHistoryView.setMeasurements(WearUserPreferences.getInstance().getAllMeasurements());
    }

    @Override
    public View getBoxInsetReferenceView() {
        return mView.getBoxInsetReferenceView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION_BODY_SENSOR) {
            if (permission.BODY_SENSORS.equals(permissions[0]) && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                toggleHeartRateMonitor();
            }
            return;
        }
    }

    @Override
    public boolean toggleHeartRateMonitor() {
        if (!PermissionUtil.requestPermission(this, REQUEST_CODE_PERMISSION_BODY_SENSOR, permission.BODY_SENSORS, null)) {
            return false;
        }

        if (!mInputLocked) {
            mInputLocked = true;
            if (!mMeasuring) {
                startHearRateMonitor();
            } else {
                stopHearRateMonitor();
                loadHistoricalData();
            }
            mInputLocked = false;
        }
        return mMeasuring;
    }
}
