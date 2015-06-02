package eu.vranckaert.heart.rate.monitor.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.util.BoxInsetLayoutUtil;
import eu.vranckaert.heart.rate.monitor.util.DateUtil;
import eu.vranckaert.heart.rate.monitor.view.HeartRateView.HeartRateListener;

import java.util.Date;

/**
 * Date: 29/05/15
 * Time: 08:02
 *
 * @author Dirk Vranckaert
 */
public class HeartRateMonitorView extends AbstractViewHolder implements OnClickListener {
    private final HeartRateListener mListener;

    private final View mContainer;
    private final TextView mTitle;
    private final TextView mBpm;
    private final View mBpmLabelContainer;
    private final TextView mTimestamp;
    private final Button mAction;

    public HeartRateMonitorView(LayoutInflater inflater, ViewGroup parent, HeartRateListener listener) {
        super(inflater, parent, R.layout.heart_rate_monitor);
        mListener = listener;
        BoxInsetLayoutUtil.setReferenceBoxInsetLayoutView(getView(), true, true, false, false, listener.getBoxInsetReferenceView());

        mContainer = findViewById(R.id.container);
        mTitle = findViewById(R.id.title);
        mBpm = findViewById(R.id.heart_rate);
        mBpmLabelContainer = findViewById(R.id.bpm_label_container);
        mTimestamp = findViewById(R.id.timestamp);
        mAction = findViewById(R.id.action);

        mBpm.setText(R.string.heart_rate_monitor_empty_heart_beat);
        mAction.setText(R.string.heart_rate_monitor_action_start);

        mAction.setOnClickListener(this);
    }

    public void setMeasuringHeartBeat(int heartBeat) {
        mTitle.setText(R.string.heart_rate_monitor_title_measuring);
        mBpm.setText("" + heartBeat);
        mAction.setText(R.string.heart_rate_monitor_action_stop);

        setMeasuringVisibility();
    }

    private void setMeasuringVisibility() {
        mBpm.setVisibility(View.VISIBLE);
        mBpmLabelContainer.setVisibility(VISIBLE);
        mTimestamp.setVisibility(INVISIBLE);
        mAction.setVisibility(VISIBLE);
    }

    public void setLatestMeasurement(Measurement measurement) {
        mAction.setText(R.string.heart_rate_monitor_action_start);
        if (measurement == null) {
            mTitle.setText(R.string.heart_rate_monitor_title_no_history);

            mBpm.setVisibility(GONE);
            mBpmLabelContainer.setVisibility(GONE);
            mTimestamp.setVisibility(INVISIBLE);
            mAction.setVisibility(VISIBLE);
        } else {
            mTitle.setText(R.string.heart_rate_monitor_title_recent);
            int heartBeat = (int) measurement.getAverageHeartBeat();
            mBpm.setText("" + heartBeat);
            mTimestamp.setText(DateUtil.formatDateTime(new Date(measurement.getStartMeasurement())));

            mBpm.setVisibility(View.VISIBLE);
            mBpmLabelContainer.setVisibility(VISIBLE);
            mTimestamp.setVisibility(VISIBLE);
            mAction.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action) {
            boolean started = mListener.toggleHeartRateMonitor();
            if (started) {
                mTitle.setText(R.string.heart_rate_monitor_title_setup);
                mBpm.setText(R.string.heart_rate_monitor_empty_heart_beat);
                mAction.setText(R.string.heart_rate_monitor_action_stop);
                setMeasuringVisibility();
            }
        }
    }
}
