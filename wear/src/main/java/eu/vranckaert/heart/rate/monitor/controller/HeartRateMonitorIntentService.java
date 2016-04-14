package eu.vranckaert.heart.rate.monitor.controller;

import android.Manifest.permission;
import android.app.IntentService;
import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.Notification.WearableExtender;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.util.Log;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.WearUserPreferences;
import eu.vranckaert.heart.rate.monitor.shared.dao.IMeasurementDao;
import eu.vranckaert.heart.rate.monitor.shared.dao.MeasurementDao;
import eu.vranckaert.heart.rate.monitor.shared.model.ActivityState;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.shared.permission.PermissionUtil;
import eu.vranckaert.heart.rate.monitor.task.HeartRateMeasurementTask;
import eu.vranckaert.heart.rate.monitor.util.DeviceUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The HeartRateMonitorIntentService get started by the repeated or one time measuring alarm and is responsible for all
 * background heart rate measurements. It does some checks to decide weater or not he can start measuring and has
 * security checks to end measurements after a maximum of {@link ActivityState#DEFAULT_MEASURING_TIMEOUT} or if values
 * are received after {@link ActivityState#DEFAULT_MEASURING_DURATION}.
 *
 * @author Dirk Vranckaert
 */
public class HeartRateMonitorIntentService extends IntentService implements SensorEventListener {
    private static final int MEASURING_NOTIFICATION = 1;

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

        if (!PermissionUtil.hasPermission(this, permission.BODY_SENSORS)) {
            stopSelf();
            return;
        }

        mStartTime = new Date().getTime();

        if (getSystemService(Context.SENSOR_SERVICE) != null) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null) {
                mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
                if (!DeviceUtil.isCharging()) {
                    startHeartRateMonitor();
                } else {
                    Log.d("dirk-background", "Will not start heart rate monitoring as device is currently charging");
                    stopSelf();
                }
            } else {
                stopSelf();
            }
        } else {
            stopSelf();
        }
    }

    /**
     * This method is run every time the sensor returns a new heart beat and it checks if maximum duration of
     * {@link ActivityState#DEFAULT_MEASURING_DURATION} milliseconds is passed or not.
     */
    private void checkDuration() {
        if (mEndTime == -1) {
            Log.d("dirk-background", "checkDuration (set initial values)");
            // Determine the end time of the measurement (aka the duration)
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MILLISECOND, ActivityState.DEFAULT_MEASURING_DURATION);
            mEndTime = calendar.getTimeInMillis();
        } else {
            Log.d("dirk-background", "checkDuration");
            long currentTime = new Date().getTime();
            if (currentTime >= mEndTime) {
                doStopHeartRateMonitor();
            }
        }
    }

    /**
     * Stop the heart rate measuring right away. If any heart rates have been measured they will be saved and
     * synchronisation to the phone will be launched for all measurements that have not yet been synced.
     */
    private void doStopHeartRateMonitor() {
        stopHeartRateMonitor();
        if (mMeasuring) {
            float heartBeat = calculateAverageHeartBeat();
            Measurement measurement = new Measurement();
            measurement.updateUniqueKey();
            measurement.setAverageHeartBeat(heartBeat);
            measurement.setMinimumHeartBeat(mMinimumHeartBeat);
            measurement.setMaximumHeartBeat(mMaximumHeartBeat);
            measurement.setStartMeasurement(mStartTime);
            measurement.setEndMeasurement(new Date().getTime());
            measurement.setFirstMeasurement(mFirstMeasurement);
            measurement.setMeasuredValues(mMeasuredValues);

            IMeasurementDao measurementDao = new MeasurementDao(this);
            measurementDao.save(measurement);
            new HeartRateMeasurementTask().execute(measurementDao.findMeasurementsToSyncWithPhone());
        }

        stopSelf();
    }

    /**
     * Calculates the average heart beat that has been measured.
     * @return The average heart beat of the measured values.
     */
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

    /**
     * Starts heart rate sensor on the device by registering a listener on the sensor manager.<br/>
     * If setup in the preferences a notification will also be shown to the user indicating that measurement has
     * started.<br/>
     * A timer also gets started with a maximum duration of {@link ActivityState#DEFAULT_MEASURING_TIMEOUT} milliseconds
     * after which the measurement will be force stopped.
     */
    private void startHeartRateMonitor() {
        Log.d("dirk-background", "startHeartRateMonitor");

        if (WearUserPreferences.getInstance().showHeartRateMeasuementNotification()) {
            Log.d("dirk", "Sending notification to user...");
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

            Notification.Builder notificationBuilder = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.notification_measuring_title))
                    .setContentText(getString(R.string.notification_measuring_message))
                    .setStyle(new BigTextStyle().bigText(getString(R.string.notification_measuring_message)))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setLargeIcon(background)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .extend(new WearableExtender().setBackground(background));
            notificationManager.notify(MEASURING_NOTIFICATION, notificationBuilder.build());
        }

        HeartRateObserver.onStartMeasuringHeartBeat();

        new CountDownTimer(ActivityState.DEFAULT_MEASURING_TIMEOUT, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!mMeasuring) {
                    // In a good case measuring will have stopped after +/- 15 seconds. If so the timer can be
                    // cancelled.
                    cancel();
                }
            }

            @Override
            public void onFinish() {
                if (mMeasuring) {
                    // If still measuring after x minutes, then measurement should be cancelled.
                    doStopHeartRateMonitor();
                }
            }
        }.start();

        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * Stops the heart rate measuring on the sensor manager and removes the notification if it has been setup in the
     * settings.
     */
    private void stopHeartRateMonitor() {
        Log.d("dirk-background", "stopHeartRateMonitor");

        if (WearUserPreferences.getInstance().showHeartRateMeasuementNotification()) {
            Log.d("dirk", "Dismissing notification...");

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(MEASURING_NOTIFICATION);
        }

        mMeasuring = false;
        mSensorManager.unregisterListener(this, mHeartRateSensor);
        HeartRateObserver.onStopMeasuringHeartBeat();
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
            HeartRateObserver.onHeartBeatMeasured(value);
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
        // Not necessary for this purpose
    }
}
