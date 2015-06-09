package eu.vranckaert.heart.rate.monitor;

import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import eu.vranckaert.hear.rate.monitor.shared.WearKeys;
import eu.vranckaert.hear.rate.monitor.shared.WearURL;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;

import java.util.List;

/**
 * Date: 12/05/15
 * Time: 16:07
 *
 * @author Dirk Vranckaert
 */
public class WearBusinessService {
    private static WearBusinessService INSTANCE;

    private GoogleApiClient mGoogleApiClient;

    public static WearBusinessService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WearBusinessService();
        }

        return INSTANCE;
    }

    private GoogleApiClient getGoogleApiClient() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient = new GoogleApiClient.Builder(WearHeartRateApplication.getContext())
                    .addApi(Wearable.API)
                    .build();
            ConnectionResult connectionResult = mGoogleApiClient.blockingConnect();
            if (!connectionResult.isSuccess()) {
                return null;
            }
        }

        return mGoogleApiClient;
    }

    private List<Node> getConnectedNodes() {
        NodeApi.GetConnectedNodesResult connectedNodesResult = Wearable.NodeApi.getConnectedNodes(getGoogleApiClient()).await();
        return connectedNodesResult.getNodes();
    }

    private void sendMessageToAllNodes(String path, byte[] bytes) {
        List<Node> nodes = getConnectedNodes();
        Log.d("dirk", "Number of connected nodes = " + nodes.size());
        for (Node node : nodes) {
            SendMessageResult messageResult = Wearable.MessageApi.sendMessage(getGoogleApiClient(), node.getId(), path, bytes).await();
            Log.d("dirk", "Message sending to node " + node.getId() + " aka \"" + node.getDisplayName() + "\" is success?" + messageResult.getStatus().isSuccess());
        }
        getGoogleApiClient().disconnect();
    }

    public void requestActivityUpdates() {
        Log.d("dirk", "Request phone to start monitoring activity");
        sendMessageToAllNodes(WearURL.URL_START_ACTIVITY_MONITORING, null);
    }

    public void registerHeartRate(Measurement measurement) {
        Log.d("dirk", "Send measured heart rate to phone");
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WearURL.HEART_RATE_MEASUREMENT);
        putDataMapReq.getDataMap().putString(WearKeys.MEASUREMENT, measurement.toJSON());
//        if (measurement.getMeasuredValues() != null && !measurement.getMeasuredValues().isEmpty()) {
//            long[] keys = new long[measurement.getMeasuredValues().size()];
//            float[] values = new float[measurement.getMeasuredValues().size()];
//
//            Set<Long> measuredValuesDates = measurement.getMeasuredValues().keySet();
//            Iterator<Long> measuredValuesDateIterator = measuredValuesDates.iterator();
//            int i = 0;
//            while (measuredValuesDateIterator.hasNext()) {
//                Long date = measuredValuesDateIterator.next();
//                keys[i] = date;
//                i++;
//            }
//
//            Collection<Float> measuredValuesValues = measurement.getMeasuredValues().values();
//            Iterator<Float> measuredValuesIterator = measuredValuesValues.iterator();
//            int j = 0;
//            while (measuredValuesIterator.hasNext()) {
//                Float value = measuredValuesIterator.next();
//                values[j] = value;
//                j++;
//            }
//
//            Log.d("dirk", "Adding all the heart rate measurement");
//            putDataMapReq.getDataMap().putLongArray(WearKeys.MEASUREMENT_VALUES_KEYS, keys);
//            putDataMapReq.getDataMap().putFloatArray(WearKeys.MEASUREMENT_VALUES_VALUES, values);
//        }
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataItemResult> pendingResult = Wearable.DataApi.putDataItem(getGoogleApiClient(), putDataReq);
        DataItemResult result = pendingResult.await();
        Log.d("dirk", "Measured heart rate synced with phone? " + result.getStatus().isSuccess());
        getGoogleApiClient().disconnect();
    }
}
