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
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import eu.vranckaert.hear.rate.monitor.shared.WearURL;
import eu.vranckaert.hear.rate.monitor.shared.model.ActivityState;
import eu.vranckaert.heart.rate.monitor.controller.ActivityRecognitionIntentService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    public void testQueryFitnessHeartRateData() {
        Log.d("dirk-background", "Querying for fitness data...");

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -24);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .read(DataType.TYPE_HEART_RATE_BPM)
//                .aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
//                        // Analogous to a "Group By" in SQL, defines how data should be aggregated.
//                        // bucketByTime allows for a time span, whereas bucketBySession would allow
//                        // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.MINUTES)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        Log.d("dirk-background", "About to read fitness data...");
        DataReadResult dataReadResult = Fitness.HistoryApi.readData(getFitnessApiClient(), readRequest).await();
        if (dataReadResult.getStatus().isSuccess()) {
            Log.d("dirk-background", "Successfully read data");
        } else {
            Log.d("dirk-background", "Could not read the data (" + dataReadResult.getStatus().getStatusCode() + " - " + dataReadResult.getStatus().getStatusMessage() +" )");
        }

        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i("dirk-background", "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                Log.i("dirk-background", "Number of datasets in bucket: " + dataSets.size());
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else {
            Log.i("dirk-background", "No data in the buckets");
        }

        if (dataReadResult.getDataSets().size() > 0) {
            Log.i("dirk-background", "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        } else {
            Log.i("dirk-background", "No data in the datasets");
        }
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i("dirk-background", "Data returned for Data type: " + dataSet.getDataType().getName());
        Log.i("dirk-background", "Number of points in dataset: " + dataSet.getDataPoints().size());

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i("dirk-background", "Data point:");
            Log.i("dirk-background", "\tType: " + dp.getDataType().getName());
            Log.i("dirk-background", "\tStart: " + new Date(dp.getStartTime(TimeUnit.MILLISECONDS)).toString());
            Log.i("dirk-background", "\tEnd: " +  new Date(dp.getEndTime(TimeUnit.MILLISECONDS)).toString());
            for(Field field : dp.getDataType().getFields()) {
                Log.i("dirk-background", "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }
}
