package eu.vranckaert.heart.rate.monitor.controller;

import android.Manifest.permission;
import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;
import eu.vranckaert.heart.rate.monitor.BusinessService;
import eu.vranckaert.heart.rate.monitor.DebugUserPreferences;
import eu.vranckaert.heart.rate.monitor.FitHelper;
import eu.vranckaert.heart.rate.monitor.HeartRateApplication;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.UserPreferences;
import eu.vranckaert.heart.rate.monitor.shared.NotificationId;
import eu.vranckaert.heart.rate.monitor.shared.WearKeys;
import eu.vranckaert.heart.rate.monitor.shared.WearURL;
import eu.vranckaert.heart.rate.monitor.shared.dao.IMeasurementDao;
import eu.vranckaert.heart.rate.monitor.shared.dao.MeasurementDao;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.shared.permission.PermissionUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Date: 01/06/15
 * Time: 14:15
 *
 * @author Dirk Vranckaert
 */
public class HeartRateMonitorWearableListenerService extends WearableListenerService {
    private IMeasurementDao mDao;

    @Override
    public void onCreate() {
        super.onCreate();
        mDao = new MeasurementDao(this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("dirk", "On data changed...");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                String path = item.getUri().getPath();
                Log.d("dirk", "... on path " + path);
                if (WearURL.HEART_RATE_MEASUREMENTS.equals((path))) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    String measurementsJson = dataMap.getString(WearKeys.MEASUREMENTS);
                    List<Measurement> measurements = Measurement.fromJSONList(measurementsJson);
                    List<String> measurementUniqueKeys = new ArrayList<>();
                    int size = measurements.size();
                    Log.d("dirk", "number of measurements is " + size);
                    BusinessService businessService = BusinessService.getInstance();
                    for (int i = 0; i < size; i++) {
                        Measurement measurement = measurements.get(i);
                        measurement.setSyncedWithGoogleFit(false);
                        Log.d("dirk", "measurement=" + measurement.toJSON());
                        List<Measurement> matchingMeasurements = mDao.findUnique(measurement);
                        measurementUniqueKeys.add(measurement.getUniqueKey());
                        if (!matchingMeasurements.isEmpty()) {
                            Log.d("dirk", "This measurement is already in the database");
                            continue;
                        }

                        Log.d("dirk", "Storing the measurement locally");
                        mDao.save(measurement);
                    }

                    HeartRateObserver.onMeasurementsSynced();
                    SyncGoogleFitMeasurementsTask.syncAllMeasurements();
                    businessService.sendHeartRateMeasurementsAck(measurementUniqueKeys);
                }
            }
        }
    }
}
