package eu.vranckaert.heart.rate.monitor.controller;

import android.Manifest.permission;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;
import eu.vranckaert.hear.rate.monitor.shared.permission.PermissionUtil;
import eu.vranckaert.heart.rate.monitor.BusinessService;
import eu.vranckaert.heart.rate.monitor.FitHelper;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.UserPreferences;
import eu.vranckaert.heart.rate.monitor.dao.IMeasurementDao;
import eu.vranckaert.heart.rate.monitor.dao.MeasurementDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 04/06/15
 * Time: 07:40
 *
 * @author Dirk Vranckaert
 */
public class MainActivity extends Activity implements OnClickListener {
    private static final int REQUEST_OAUTH = 1;
    private static final int REQUEST_SUBSCRIPTION = 2;
    private static final int REQUEST_CODE_PERMISSON_BODY_SENSORS = 3;
    private static final int VIEW_STATE_GOOGLE_FIT_CONNECTION = 0;
    private static final int VIEW_STATE_MEASUREMENT_LIST = 1;

    private int mCurrentViewSate = -1;

    private TextView mGoogleFitExplanation;
    private Button mConnect;
    private MeasurementsAdapter mMeasurementsAdapter;

    private GoogleApiClient mGoogleApiClient;
    private LoadMeasurementsTask mLoadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupView();
    }

    private void setupView() {
        if (!checkPermissions()) {
            setContentView(R.layout.heart_rate_permissions);

            findViewById(R.id.open_settings).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            });
        } else {
            mMeasurementsAdapter = new MeasurementsAdapter(this);
            initScreen(false);
            new CheckGoogleFitnessConnectionTask(this).execute();
        }
    }

    private boolean checkPermissions() {
        return PermissionUtil.requestPermission(this, REQUEST_CODE_PERMISSON_BODY_SENSORS, permission.BODY_SENSORS,
                getString(R.string.permission_explanation_body_sensors));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSON_BODY_SENSORS) {
            if (permission.BODY_SENSORS.equals(permissions[0]) &&
                    PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                setupView();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.disconnect) {
            cancelSubscription();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.connect) {
            setupGoogleFit();
        }
    }

    /**
     * Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs. The scopes included should match the scopes your app needs
     * (see documentation for details). Authentication will occasionally fail intentionally,
     * and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     * can address. Examples of this include the user never having signed in before, or having
     * multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void setupGoogleFit() {
        // TODO is not yet a progress dialog with a spinning wheel
        final AlertDialog progress = new ProgressDialog.Builder(this)
                .setMessage(R.string.google_fit_setup_connecting)
                .setCancelable(false)
                .show();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.RECORDING_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i("dirk", "Connected!!!");
                                progress.dismiss();
                                // Now you can make calls to the Fitness APIs.  What to do?
                                // Subscribe to some data sources!
                                subscribe();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                progress.dismiss();
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.e("dirk", "Connection lost.  Cause: Network Lost.");
                                    // TODO show error handling
                                } else if (i == ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.e("dirk", "Connection lost.  Reason: Service Disconnected");
                                    // TODO show error handling
                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Log.i("dirk", "Connection failed. Cause: " + result.toString());
                                progress.dismiss();
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), MainActivity.this, 0)
                                            .show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                try {
                                    Log.i("dirk", "Attempting to resolve failed connection");
                                    result.startResolutionForResult(MainActivity.this, REQUEST_OAUTH);
                                } catch (IntentSender.SendIntentException e) {
                                    Log.e("dirk", "Exception while starting resolution activity", e);
                                    // TODO show error handling
                                }
                            }
                        }
                )
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Subscribe to an available {@link DataType}. Subscriptions can exist across application
     * instances (so data is recorded even after the application closes down).  When creating
     * a new subscription, it may already exist from a previous invocation of this app.  If
     * the subscription already exists, the method is a no-op.  However, you can check this with
     * a special success code.
     */
    public void subscribe() {
        // TODO is not yet a progress dialog with a spinning wheel
        final AlertDialog progress = new ProgressDialog.Builder(this)
                .setMessage(R.string.google_fit_setup_setup_fit)
                .setCancelable(false)
                .show();

        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.RecordingApi.subscribe(mGoogleApiClient, FitHelper.DATA_TYPE_HEART_RATE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        progress.dismiss();

                        if (status.isSuccess()) {
                            if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i("dirk", "Existing subscription for activity detected.");
                            } else {
                                Log.i("dirk", "Successfully subscribed!");
                            }
                            UserPreferences.getInstance().setGoogleFitConnected(true);
                            initScreen(false);
                        } else {
                            Log.i("dirk", "There was a problem subscribing.");
                            UserPreferences.getInstance().setGoogleFitConnected(false);
                            if (status.hasResolution()) {
                                try {
                                    status.startResolutionForResult(MainActivity.this, REQUEST_SUBSCRIPTION);
                                } catch (SendIntentException e) {
                                    Log.e("dirk", "Exception while starting resolution activity", e);
                                }
                            } else {
                                GooglePlayServicesUtil.getErrorDialog(status.getStatusCode(), MainActivity.this, 0)
                                        .show();
                            }
                        }
                    }
                });
    }

    /**
     * Cancel the ACTIVITY_SAMPLE subscription by calling unsubscribe on that {@link DataType}.
     */
    private void cancelSubscription() {
        new CancelGoogleFitSubscriptions(this).execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            }
        } else if (requestCode == REQUEST_SUBSCRIPTION) {
            if (resultCode == RESULT_OK) {
                subscribe();
            }
        }
    }

    private void initScreen(boolean checkPerformed) {
        invalidateOptionsMenu();
        boolean hasGoogleFitConnection = UserPreferences.getInstance().getGoogleFitConnected();
        if (checkPerformed) {
            new PhoneSetupTask().execute(hasGoogleFitConnection);
        }
        if (!hasGoogleFitConnection) {
            initGoogleFitConnectionScreen(checkPerformed);
        } else {
            initOverviewOfMeasurements();
        }
    }

    private void initGoogleFitConnectionScreen(boolean checkPerformed) {
        mCurrentViewSate = VIEW_STATE_GOOGLE_FIT_CONNECTION;
        setContentView(R.layout.google_fit_connection);
        mGoogleFitExplanation = (TextView) findViewById(R.id.fit_explanation);
        mConnect = (Button) findViewById(R.id.connect);
        mConnect.setOnClickListener(this);
        mConnect.setEnabled(checkPerformed);
        mGoogleFitExplanation.setText(R.string.google_fit_setup_disconnected_summary);
    }

    @Override
    protected void onDestroy() {
        if (mLoadTask != null) {
            mLoadTask.cancel(true);
            mLoadTask = null;
        }

        super.onDestroy();
    }

    private void initOverviewOfMeasurements() {
        mCurrentViewSate = VIEW_STATE_MEASUREMENT_LIST;
        setContentView(R.layout.heart_rate_measurements);
        setMeasurements(new ArrayList<>());
        if (mLoadTask != null) {
            mLoadTask.cancel(true);
        }
        mLoadTask = new LoadMeasurementsTask();
        mLoadTask.execute();
    }

    private void setMeasurements(List<Measurement> measurements) {
        if (mCurrentViewSate == VIEW_STATE_MEASUREMENT_LIST) {
            ListView list = (ListView) findViewById(R.id.list);
            list.setAdapter(mMeasurementsAdapter);
            mMeasurementsAdapter.setMeasurements(measurements);
            TextView empty = (TextView) findViewById(R.id.empty);
            if (measurements == null || measurements.isEmpty()) {
                list.setVisibility(View.GONE);
                empty.setVisibility(View.VISIBLE);
            } else {
                list.setVisibility(View.VISIBLE);
                empty.setVisibility(View.GONE);
            }
        }
    }

    private class LoadMeasurementsTask extends AsyncTask<Void, Void, List<Measurement>> {

        @Override
        protected List<Measurement> doInBackground(Void... params) {
            // Retrieve all measurements and filter out the fake heart rate measurements
            IMeasurementDao dao = new MeasurementDao();
            List<Measurement> measurements = dao.findAllSorted();
            //            List<Measurement> fakeMeasurements = new ArrayList<>();
            //            int measurementCount = measurements.size();
            //            for (int i=0; i<measurementCount; i++) {
            //                Measurement measurement = measurements.get(i);
            //                if (measurement.isFakeHeartRate()) {
            //                    fakeMeasurements.add(measurement);
            //                }
            //            }
            //            measurements.removeAll(fakeMeasurements);
            return measurements;
        }

        @Override
        protected void onPostExecute(List<Measurement> measurements) {
            setMeasurements(measurements);
        }
    }

    private class CheckGoogleFitnessConnectionTask extends AsyncTask<Void, Void, Boolean> {
        private final MainActivity mActivity;

        public CheckGoogleFitnessConnectionTask(MainActivity activity) {
            mActivity = activity;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return BusinessService.getInstance().hasFitnessSubscription(FitHelper.DATA_TYPE_HEART_RATE);
        }

        @Override
        protected void onPostExecute(Boolean hasGoogleFitConnection) {
            UserPreferences.getInstance().setGoogleFitConnected(hasGoogleFitConnection);
            mActivity.initScreen(true);
        }
    }

    private class CancelGoogleFitSubscriptions extends AsyncTask<Void, Void, Void> {
        private final MainActivity mActivity;
        private AlertDialog mProgress;

        public CancelGoogleFitSubscriptions(MainActivity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            mProgress = new ProgressDialog.Builder(mActivity)
                    .setMessage(R.string.google_fit_setup_setup_fit)
                    .setCancelable(false)
                    .show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            BusinessService businessService = BusinessService.getInstance();
            businessService.setPhoneSetupCompletionStatus(false);
            businessService.cancelFitnessSubscription(FitHelper.DATA_TYPE_HEART_RATE);
            UserPreferences.getInstance().setGoogleFitConnected(false);
            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            mProgress.dismiss();
            mActivity.initScreen(false);
        }
    }

    private class PhoneSetupTask extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... params) {
            BusinessService.getInstance().setPhoneSetupCompletionStatus(params[0]);
            return null;
        }
    }
}
