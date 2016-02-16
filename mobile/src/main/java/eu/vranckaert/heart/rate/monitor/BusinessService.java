package eu.vranckaert.heart.rate.monitor;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import eu.vranckaert.heart.rate.monitor.controller.ActivityRecognitionIntentService;
import eu.vranckaert.heart.rate.monitor.shared.WearKeys;
import eu.vranckaert.heart.rate.monitor.shared.WearURL;
import eu.vranckaert.heart.rate.monitor.shared.model.ActivityState;

import java.util.ArrayList;
import java.util.Date;
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
                    .build();
            ConnectionResult connectionResult = googleApiClient.blockingConnect();
            if (connectionResult.isSuccess()) {
                Log.d("dirk-background", "mActivityRecognitionApiClient connected");
                mActivityRecognitionApiClient = googleApiClient;
                connectActivityRecognitionApiClient();
            }

            return null;
        } else {
            Log.d("dirk-background",
                    "mActivityRecognitionApiClient.connected=" + mActivityRecognitionApiClient.isConnected());
            return mActivityRecognitionApiClient;
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

        if (getActivityRecognitionApiClient() != null) {
            PendingResult<Status> pendingResult = ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                    mActivityRecognitionApiClient,
                    ActivityState.DETECTION_INTERVAL,
                    pendingIntent
            );
            Status status = pendingResult.await();
            Log.d("dirk-background", "status.success=" + status.isSuccess());
            Log.d("dirk-background", "status.statusMessage=" + status.getStatusMessage());
            Log.d("dirk-background", "status.statusCode=" + status.getStatusCode());
        } else {
            Log.d("dirk-background", "Google api client for activity recognition is null");
        }
    }

    public void setActivityUpdate(int activityState) {
        List<Node> nodes = getConnectedNodes();
        for (Node node : nodes) {
            Wearable.MessageApi.sendMessage(getWearableGoogleApiClient(), node.getId(), WearURL.URL_ACTIVITY_MONITORING_RESULT + activityState, null);
        }
        getWearableGoogleApiClient().disconnect();
    }

    public void setPhoneSetupCompletionStatus(boolean completed) {
        String url = completed ? WearURL.URL_SETUP_COMPLETED : WearURL.URL_SETUP_UNCOMPLETED;

        List<Node> nodes = getConnectedNodes();
        for (Node node : nodes) {
            Wearable.MessageApi.sendMessage(getWearableGoogleApiClient(), node.getId(), url, null);
        }
        getWearableGoogleApiClient().disconnect();
    }

    public boolean hasFitnessSubscription(DataType type) {
        Log.d("dirk-background", "Checking if subscription is available for " + type.getName());
        GoogleApiClient googleApiClient = getFitnessApiClient();
        if (googleApiClient == null) {
            Log.d("dirk-background", "Could not get fitness API client...");
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
        Log.d("dirk-background", "No fitness subscriptions found");
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

    public boolean addFitnessHeartRateMeasurement(DataSet dataSet) {
        Log.d("dirk-background", "Adding a new fitness heart rate measurement...");
        Status status = Fitness.HistoryApi.insertData(getFitnessApiClient(), dataSet).await();
        Log.d("dirk-background", "Adding the heart rate measurement to Fitness was success? " + status.isSuccess());

        return status.isSuccess();
    }

    public void sendHeartRateMeasurementsAck(List<String> measurementUniqueKeys) {
        Log.d("dirk-ack", "Send measured heart rate ACK to watch");
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WearURL.HEART_RATE_MEASUREMENTS_ACK);
        putDataMapReq.getDataMap().putStringArrayList(WearKeys.MEASUREMENT_KEYS, (ArrayList<String>) measurementUniqueKeys);
        putDataMapReq.getDataMap().putLong("timestamp", new Date().getTime());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataItemResult> pendingResult = Wearable.DataApi.putDataItem(getWearableGoogleApiClient(), putDataReq);
        DataItemResult result = pendingResult.await();
        Log.d("dirk-ack", "Measured heart rates ACK sent to watch? " + result.getStatus().isSuccess());
        getWearableGoogleApiClient().disconnect();
    }
}
