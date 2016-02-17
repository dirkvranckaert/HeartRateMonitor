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
import eu.vranckaert.heart.rate.monitor.shared.WearKeys;
import eu.vranckaert.heart.rate.monitor.shared.WearURL;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;

import java.util.Date;
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

    public boolean registerHeartRates(List<Measurement> measurements) {
        Log.d("dirk", "Send measured heart rate to phone");
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WearURL.HEART_RATE_MEASUREMENTS);
        putDataMapReq.getDataMap().putString(WearKeys.MEASUREMENTS, Measurement.toJSONList(measurements));
        putDataMapReq.getDataMap().putLong("timestamp", new Date().getTime());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataItemResult> pendingResult = Wearable.DataApi.putDataItem(getGoogleApiClient(), putDataReq);
        DataItemResult result = pendingResult.await();
        boolean success = result.getStatus().isSuccess();
        Log.d("dirk", "Measured heart rates synced with phone? " + success);
        getGoogleApiClient().disconnect();
        return success;
    }
}
