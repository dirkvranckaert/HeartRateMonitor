package eu.vranckaert.heart.rate.monitor.controller;

import android.util.Log;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import eu.vranckaert.heart.rate.monitor.WearUserPreferences;
import eu.vranckaert.heart.rate.monitor.shared.WearKeys;
import eu.vranckaert.heart.rate.monitor.shared.WearURL;
import eu.vranckaert.heart.rate.monitor.shared.dao.IMeasurementDao;
import eu.vranckaert.heart.rate.monitor.shared.dao.MeasurementDao;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;

import java.util.List;

/**
 * Date: 01/06/15
 * Time: 14:15
 *
 * @author Dirk Vranckaert
 */
public class HearRateMonitorWearableListenerService extends WearableListenerService {
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("dirk", "On data changed...");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                String path = item.getUri().getPath();
                Log.d("dirk", "... on path " + path);
                if (WearURL.HEART_RATE_MEASUREMENTS_ACK.equals((path))) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    List<String> measurementKeys = dataMap.getStringArrayList(WearKeys.MEASUREMENT_KEYS);
                    Log.d("dirk", "Number of ACK measurement keys received: " + measurementKeys.size());
                    IMeasurementDao measurementDao = new MeasurementDao(this);
                    List<Measurement> measurements = measurementDao.findAllByUniqueKey(measurementKeys);
                    Log.d("dirk", "Number of matching ACK measurements found: " + measurements.size());
                    int size = measurements.size();
                    for (int i = 0; i < size; i++) {
                        Measurement measurement = measurements.get(i);
                        measurement.setSyncedWithPhone(true);
                        measurementDao.update(measurement);
                    }

                    HeartRateObserver.onMeasurementsAckReceived();
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        Log.d("dirk", "Message received on " + path);

        final WearUserPreferences preferences = WearUserPreferences.getInstance();
        switch (path) {
            case WearURL.URL_SETUP_COMPLETED:
                preferences.setPhoneSetupCompleted(true);
                preferences.setHasntRunBefore();
                SetupBroadcastReceiver.setupMeasuring(this);
                ConfigObserver.onPhoneSetupCompletionChanged();
                break;
            case WearURL.URL_SETUP_UNCOMPLETED:
                preferences.setPhoneSetupCompleted(false);
                preferences.setHasntRunBefore();
                ConfigObserver.onPhoneSetupCompletionChanged();
                break;
        }
    }
}
