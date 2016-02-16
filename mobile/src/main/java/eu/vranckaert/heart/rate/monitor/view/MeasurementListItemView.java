package eu.vranckaert.heart.rate.monitor.view;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.vranckaert.heart.rate.monitor.shared.model.ActivityState;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.shared.util.DateUtil;
import eu.vranckaert.heart.rate.monitor.R;

import java.util.Date;
import java.util.Map;

/**
 * Date: 02/09/15
 * Time: 16:49
 *
 * @author Dirk Vranckaert
 */
public class MeasurementListItemView extends AbstractViewHolder {
    private final TextView mBpm;
    private final TextView mDuration;
    private final TextView mDate;
    private final TextView mTime;
    private final TextView mActivity;
    private final TextView mFitSyncState;
    private final TextView mFakeHeartRate;

    public MeasurementListItemView(LayoutInflater inflater, ViewGroup parent) {
        super(inflater, parent, R.layout.heart_rate_history_item);

        mBpm = findViewById(R.id.bpm);
        mDuration = findViewById(R.id.duration);
        mDate = findViewById(R.id.date);
        mTime = findViewById(R.id.time);
        mActivity = findViewById(R.id.activity);
        mFitSyncState = findViewById(R.id.fit_sync_state);
        mFakeHeartRate = findViewById(R.id.fake_heart_rate);
    }

    public void setMeasurement(Measurement measurement) {
        int heartBeat = (int) measurement.getAverageHeartBeat();
        mBpm.setText("" + heartBeat);
        Date start = new Date(measurement.getStartMeasurement());
        mDate.setText(DateUtil.formatDate(start));
        mTime.setText(DateUtil.formatTime(start));
        long duration = measurement.getEndMeasurement() - measurement.getStartMeasurement();
        Map<String, Long> calculatedDuration = DateUtil.calculateDuration(duration, DateUtil.DURATION_TYPE_MINUTES, DateUtil.DURATION_TYPE_SECONDS);
        String durationText = "" + calculatedDuration.get(DateUtil.MINUTES) + ":";
        long minutes = calculatedDuration.get(DateUtil.SECONDS);
        if (minutes < 10) {
            durationText += "0";
        }
        durationText += "" + minutes;

        mDuration.setText(durationText);

        mActivity.setText(measurement.getActivityName(getContext()));

        mFitSyncState.setText(measurement.isSyncedWithGoogleFit() ? "Synced to Google Fit" : "Not yet synced to Google Fit");

        String fakeHeartRate = measurement.detectFakeHeartRate();
        mFakeHeartRate.setText(fakeHeartRate);
        mFakeHeartRate.setVisibility(TextUtils.isEmpty(fakeHeartRate) ? GONE : VISIBLE);
    }
}
