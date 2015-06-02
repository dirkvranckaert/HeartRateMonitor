package eu.vranckaert.heart.rate.monitor;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import eu.vranckaert.hear.rate.monitor.shared.WearURL;
import eu.vranckaert.heart.rate.monitor.controller.ActivityRecognitionIntentService;

import java.util.List;

/**
 * Date: 12/05/15
 * Time: 16:07
 *
 * @author Dirk Vranckaert
 */
public class BusinessService {
    private static BusinessService INSTANCE;
    private GoogleApiClient mWearableGoogleApiClient;
    private GoogleApiClient mActivityRecognitionApiClient;

    public static BusinessService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BusinessService();
        }

        return INSTANCE;
    }

    private GoogleApiClient getActivityRecognitionApiClient() {
        if (mActivityRecognitionApiClient == null || !mActivityRecognitionApiClient.isConnected()){
            Log.d("dirk-background", "No mActivityRecognitionApiClient or not connected anymore");
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(HearRateApplication.getContext())
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(new ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.d("dirk-background", "mActivityRecognitionApiClient suspended, restarting connectActivityRecognitionApiClient");
                            connectActivityRecognitionApiClient();
                        }
                    })
                    .build();
            ConnectionResult connectionResult = googleApiClient.blockingConnect();
            if (connectionResult.isSuccess()) {
                Log.d("dirk-background", "mActivityRecognitionApiClient connected");
                mActivityRecognitionApiClient = googleApiClient;
            }
        }

        Log.d("dirk-background", "mActivityRecognitionApiClient.connected=" + mActivityRecognitionApiClient.isConnected());
        if (mActivityRecognitionApiClient.isConnected()) {
            return mActivityRecognitionApiClient;
        } else {
            return null;
        }
    }

    private GoogleApiClient getWearableGoogleApiClient() {
        if (mWearableGoogleApiClient == null || !mWearableGoogleApiClient.isConnected()) {
            mWearableGoogleApiClient = new GoogleApiClient.Builder(HearRateApplication.getContext())
                    .addApi(Wearable.API)
                    .build();
            ConnectionResult connectionResult = mWearableGoogleApiClient.blockingConnect();
            if (!connectionResult.isSuccess()) {
                return null;
            }
        }

        return mWearableGoogleApiClient;
    }

    private List<Node> getConnectedNodes() {
        NodeApi.GetConnectedNodesResult connectedNodesResult = Wearable.NodeApi.getConnectedNodes(
                getWearableGoogleApiClient()).await();
        return connectedNodesResult.getNodes();
    }

    public void connectActivityRecognitionApiClient() {
        Intent intent = new Intent(HearRateApplication.getContext(), ActivityRecognitionIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(HearRateApplication.getContext(), 0, intent, 0);

        PendingResult<Status> pendingResult = ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                getActivityRecognitionApiClient(),
                0,
                pendingIntent
        );
        Status status = pendingResult.await();
        Log.d("dirk-background", "status.success=" + status.isSuccess());
        Log.d("dirk-background", "status.statusMessage=" + status.getStatusMessage());
        Log.d("dirk-background", "status.statusCode=" + status.getStatusCode());
    }

    public void setActivityUpdate(int activityState) {
        List<Node> nodes = getConnectedNodes();
        for (Node node : nodes) {
            Wearable.MessageApi.sendMessage(getWearableGoogleApiClient(), node.getId(), WearURL.URL_ACTIVITY_MONITORING_RESULT + activityState, null);
        }
        getWearableGoogleApiClient().disconnect();
    }
}
