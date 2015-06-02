package eu.vranckaert.heart.rate.monitor;

import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import eu.vranckaert.hear.rate.monitor.shared.WearURL;

import java.util.List;

/**
 * Date: 12/05/15
 * Time: 16:07
 *
 * @author Dirk Vranckaert
 */
public class BusinessService {
    private static BusinessService INSTANCE;

    private GoogleApiClient mGoogleApiClient;

    public static BusinessService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BusinessService();
        }

        return INSTANCE;
    }

    private GoogleApiClient getGoogleApiClient() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient = new GoogleApiClient.Builder(HearRateApplication.getContext())
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

    public void requestActivityUpdates() {
        Log.d("dirk", "Request phone to start monitoring activity");
        List<Node> nodes = getConnectedNodes();
        Log.d("dirk", "Number of connected nodes = " + nodes.size());
        for (Node node : nodes) {
            SendMessageResult messageResult = Wearable.MessageApi.sendMessage(getGoogleApiClient(), node.getId(), WearURL.URL_START_ACTIVITY_MONITORING, null).await();
            Log.d("dirk", "Message sending to node " + node.getId() + " aka \"" + node.getDisplayName() + "\" is success?" + messageResult.getStatus().isSuccess());
        }
        getGoogleApiClient().disconnect();
    }
}
