package eu.vranckaert.heart.rate.monitor;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import eu.vranckaert.hear.rate.monitor.shared.WearURL;
import eu.vranckaert.hear.rate.monitor.shared.model.ActivityState;
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
    private GoogleApiClient mFitnessApiClient;

    public static BusinessService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BusinessService();
        }

        return INSTANCE;
    }

    public GoogleApiClient getFitnessApiClient() {
        if (mFitnessApiClient == null || !mFitnessApiClient.isConnected()) {
            Log.d("dirk-background", "No mFitnessApiClient or not connected anymore");
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(HeartRateApplication.getContext())
                    .addApi(Fitness.HISTORY_API)
                    .addApi(Fitness.RECORDING_API)
                    .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                    .build();
            ConnectionResult connectionResult = googleApiClient.blockingConnect();
            if (connectionResult.isSuccess()) {
                Log.d("dirk-background", "mFitnessApiClient connected");
                mFitnessApiClient = googleApiClient;
            } else {
                Log.d("dirk-background", "mFitnessApiClient connection failed: " + connectionResult.getErrorCode());
            }
        }

        Log.d("dirk-background", "mFitnessApiClient.connected=" + (mFitnessApiClient != null && mFitnessApiClient.isConnected()));
        if (mFitnessApiClient != null && mFitnessApiClient.isConnected()) {
            return mFitnessApiClient;
        } else {
            return null;
        }
    }

    private GoogleApiClient getActivityRecognitionApiClient() {
        if (mActivityRecognitionApiClient == null || !mActivityRecognitionApiClient.isConnected()){
            Log.d("dirk-background", "No mActivityRecognitionApiClient or not connected anymore");
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(HeartRateApplication.getContext())
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
            mWearableGoogleApiClient = new GoogleApiClient.Builder(HeartRateApplication.getContext())
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
        Intent intent = new Intent(HeartRateApplication.getContext(), ActivityRecognitionIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(HeartRateApplication.getContext(), 0, intent, 0);

        PendingResult<Status> pendingResult = ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                getActivityRecognitionApiClient(),
                ActivityState.DETECTION_INTERVAL,
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

    public boolean hasFitnessSubscription(DataType type) {
        Log.d("dirk-background", "Checking if subscription is available for " + type.getName());
        GoogleApiClient googleApiClient = getFitnessApiClient();
        if (googleApiClient == null) {
            return false;
        }

        ListSubscriptionsResult listSubscriptionsResult = Fitness.RecordingApi.listSubscriptions(googleApiClient, type).await();
        if (listSubscriptionsResult.getStatus().isSuccess()) {
            Log.d("dirk-background", "listSubscriptionsResult.subscriptions.size=" + listSubscriptionsResult.getSubscriptions().size());
            for (Subscription subscription : listSubscriptionsResult.getSubscriptions()) {
                DataType dataType = subscription.getDataType();
                if (dataType.equals(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void cancelFitnessSubscription(DataType type) {
        Log.d("dirk-background", "Cancelling subscription " + type.getName());
        GoogleApiClient googleApiClient = getFitnessApiClient();
        if (googleApiClient == null) {
            return;
        }

        Status status = Fitness.RecordingApi.unsubscribe(googleApiClient, DataType.TYPE_ACTIVITY_SAMPLE).await();
        if (status.isSuccess()) {
            Log.i("dirk-background", "Successfully unsubscribed for data type: " + type.toString());
        } else {
            // Subscription not removed
            Log.i("dirk-background", "Failed to unsubscribe for data type: " + type.toString());
        }
    }

    public void addFitnessHeartRateMeasurement(DataSet dataSet) {
        Log.d("dirk-background", "Adding a new fitness heart rate measurement...");
        Status status = Fitness.HistoryApi.insertData(getFitnessApiClient(), dataSet).await();
        Log.d("dirk-background", "Adding the heart rate measurement to Fitness was success? " + status.isSuccess());
    }
}
