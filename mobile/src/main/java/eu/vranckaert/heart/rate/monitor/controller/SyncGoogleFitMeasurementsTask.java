package eu.vranckaert.heart.rate.monitor.controller;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.Field;
import eu.vranckaert.heart.rate.monitor.BusinessService;
import eu.vranckaert.heart.rate.monitor.DebugUserPreferences;
import eu.vranckaert.heart.rate.monitor.FitHelper;
import eu.vranckaert.heart.rate.monitor.HeartRateApplication;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.UserPreferences;
import eu.vranckaert.heart.rate.monitor.shared.NotificationId;
import eu.vranckaert.heart.rate.monitor.shared.dao.IMeasurementDao;
import eu.vranckaert.heart.rate.monitor.shared.dao.MeasurementDao;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.shared.permission.PermissionUtil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Date: 22/02/16
 * Time: 15:53
 *
 * @author Dirk Vranckaert
 */
public class SyncGoogleFitMeasurementsTask extends AsyncTask<Void, Void, Void> {
    private final Context mContext;
    private AlertDialog mProgress;

    public SyncGoogleFitMeasurementsTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        mProgress = ProgressDialog.show(mContext, null, mContext.getString(R.string.sync_now_loading), true, false);
    }

    @Override
    protected Void doInBackground(Void... params) {
        syncAllMeasurements();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mProgress.dismiss();
    }

    public static void syncAllMeasurements() {
        boolean canSyncWithGoogleFit = DebugUserPreferences.getInstance().canSyncWithGoogleFit();
        if (!canSyncWithGoogleFit) {
            Log.d("dirk", "Skip Google Fit sync (disabled in dev settings)");
            HeartRateObserver.onMeasurementsSynced();
            return;
        }

        Context context = HeartRateApplication.getContext();
        boolean hasPermissions = PermissionUtil.hasPermission(context, permission.BODY_SENSORS);
        if (!hasPermissions) {
            Log.d("dirk", "Skip Google Fit sync (missing permission " + permission.BODY_SENSORS + ")");
            return;
        }

        final BusinessService businessService = BusinessService.getInstance();
        final UserPreferences userPreferences = UserPreferences.getInstance();

        boolean hasAggregateHeartRateSummarySubscription =
                businessService.hasFitnessSubscription(FitHelper.DATA_TYPE_HEART_RATE);
        if (!hasAggregateHeartRateSummarySubscription) {
            userPreferences.setGoogleFitConnected(false);
            businessService.setPhoneSetupCompletionStatus(false);

            Log.d("dirk", "No Google Fitness subscriptions yet...");
            int errorCount = userPreferences.getGoogleFitActivationErrorCount();
            Log.d("dirk", "Google fit activation error occurred " + errorCount + " time(s) now");
            if (errorCount == 10) {
                Log.d("dirk", "Showing notification to logon to Google Fit");
                userPreferences.setGoogleFitActivationErrorCount(0);

                Intent intent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(context, 0, intent, 0);

                Notification notification =
                        new Notification.Builder(context)
                                .setContentTitle(context
                                        .getString(R.string.app_name))
                                .setContentText(context
                                        .getString(
                                                R.string.notification_google_fitness_not_connected_message))
                                .setStyle(
                                        new BigTextStyle().bigText(context
                                                .getString(
                                                        R.string.notification_google_fitness_not_connected_message)))
                                .setSmallIcon(R.drawable.ic_notification)
                                .setColor(context.getResources()
                                        .getColor(R.color.hrm_accent_color))
                                .addAction(R.drawable.fit_notification,
                                        context.getString(
                                                R.string.notification_google_fitness_not_connected_message_action_connect),
                                        pendingIntent)
                                .build();
                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NotificationId.GOOGLE_FITNESS_NOT_CONNECTED, notification);
            } else {
                Log.d("dirk", "Not showing notification to logon to Google Fit right now");
                userPreferences.setGoogleFitActivationErrorCount(errorCount + 1);
            }
            return;
        }


        IMeasurementDao mDao = new MeasurementDao(context);
        List<Measurement> measurements = mDao.findMeasurementsToSyncWithFit();
        Log.d("dirk", "Syncing all postponed measurements, found " + measurements.size() + " measurement(s)");
        int syncCount = 0;
        for (int i = 0; i < measurements.size(); i++) {
            Measurement measurement = measurements.get(i);
            if (!measurement.isFakeHeartRate()) {
                UserPreferences.getInstance().setGoogleFitActivationErrorCount(-1);
                DataSet dataSet = getGoogleFitDataSet(measurement);
                boolean success = businessService.addFitnessHeartRateMeasurement(dataSet);
                Log.d("dirk", "Measurement from " + new Date(measurement.getStartMeasurement()) +
                        " has been synced with Google Fit. Result is success? " + success);
                if (success) {
                    Log.d("dirk", "Storing the BPM in Google Fit was successful, updating local DB");
                    measurement.setSyncedWithGoogleFit(true);
                    mDao.update(measurement);
                    syncCount++;
                } else {
                    Log.d("dirk", "Could not store BPM in Google Fit, updating local DB");
                    measurement.setSyncedWithGoogleFit(false);
                    mDao.update(measurement);
                }
            } else {
                Log.d("dirk", "Fake heart rate! Reason:" + measurement.detectFakeHeartRate());
            }
        }

        if (syncCount > 0) {
            HeartRateObserver.onMeasurementsSynced();
        }
    }

    @NonNull
    private static DataSet getGoogleFitDataSet(Measurement measurement) {
        Log.d("dirk", "Building DataSource");
        DataSource dataSource = new DataSource.Builder()
                .setDataType(FitHelper.DATA_TYPE_HEART_RATE)
                .setType(DataSource.TYPE_RAW)
                .setAppPackageName(HeartRateApplication.getContext())
                .build();
        Log.d("dirk", "Building DataSet");
        DataSet dataSet = DataSet.create(dataSource);
        Log.d("dirk", "Building DataPoint");
        DataPoint dataPoint = dataSet.createDataPoint();
        dataPoint.setTimeInterval(measurement.getStartMeasurement(), measurement.getEndMeasurement(),
                TimeUnit.MILLISECONDS);
        //dataPoint.getValue(Field.FIELD_MIN).setFloat(measurement.getMinimumHeartBeat());
        dataPoint.getValue(Field.FIELD_BPM).setFloat(measurement.getAverageHeartBeat());
        //dataPoint.getValue(Field.FIELD_AVERAGE).setFloat(measurement.getAverageHeartBeat());
        //dataPoint.getValue(Field.FIELD_MAX).setFloat(measurement.getMaximumHeartBeat());
        dataSet.add(dataPoint);
        return dataSet;
    }
}
