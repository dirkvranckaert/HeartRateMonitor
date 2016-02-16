package eu.vranckaert.heart.rate.monitor.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;
import eu.vranckaert.hear.rate.monitor.shared.util.DateUtil;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.WearUserPreferences;
import eu.vranckaert.heart.rate.monitor.util.BoxInsetLayoutUtil;
import eu.vranckaert.heart.rate.monitor.view.HeartRateView.HeartRateListener;

import java.util.Date;

/**
 * Date: 29/05/15
 * Time: 08:02
 *
 * @author Dirk Vranckaert
 */
public class HeartRateMonitorView extends AbstractViewHolder implements OnClickListener {
    private static final int DEFAULT_HEART_BEATING_ANIMATION_BPM = 75;

    private final HeartRateListener mListener;

    private final TextView mTitle;
    private final View mHeartImage;
    private final TextView mBpm;
    private final View mBpmLabelContainer;
    private final TextView mTimestamp;
    private final TextView mCurrentActivity;
    private final Button mAction;

    private Animator mBeatingAnimation;
    private boolean mBeating = false;
    private int mBeatingBpm;

    public HeartRateMonitorView(LayoutInflater inflater, ViewGroup parent, HeartRateListener listener) {
        super(inflater, parent, R.layout.heart_rate_monitor);
        mListener = listener;
        BoxInsetLayoutUtil.setReferenceBoxInsetLayoutView(getView(), true, true, false, false,
                listener.getBoxInsetReferenceView());

        mTitle = findViewById(R.id.title);
        mHeartImage = findViewById(R.id.heart_rate_img);
        mBpm = findViewById(R.id.heart_rate);
        mBpmLabelContainer = findViewById(R.id.bpm_label_container);
        mTimestamp = findViewById(R.id.timestamp);
        mCurrentActivity = findViewById(R.id.current_activity);
        mAction = findViewById(R.id.action);

        mBpm.setText(R.string.heart_rate_monitor_empty_heart_beat);
        mAction.setText(R.string.heart_rate_monitor_action_start);

        mAction.setOnClickListener(this);
    }

    public void setMeasuringHeartBeat(int heartBeat) {
        mBeatingBpm = heartBeat;
        mTitle.setText(R.string.heart_rate_monitor_title_measuring);
        mBpm.setText("" + heartBeat);
        mAction.setText(R.string.heart_rate_monitor_action_stop);

        setMeasuringVisibility();
    }

    private void setMeasuringVisibility() {
        mBpm.setVisibility(View.VISIBLE);
        mBpmLabelContainer.setVisibility(VISIBLE);
        mTimestamp.setVisibility(INVISIBLE);
        mCurrentActivity.setVisibility(VISIBLE);
        mAction.setVisibility(VISIBLE);
    }

    public void setLatestMeasurement(Measurement measurement) {
        mAction.setText(R.string.heart_rate_monitor_action_start);
        if (measurement == null) {
            mTitle.setText(R.string.heart_rate_monitor_title_no_history);
            mBpm.setVisibility(GONE);
            mBpmLabelContainer.setVisibility(GONE);
            mTimestamp.setVisibility(INVISIBLE);
            mCurrentActivity.setVisibility(INVISIBLE);
            mAction.setVisibility(VISIBLE);
        } else {
            mTitle.setText(R.string.heart_rate_monitor_title_recent);
            int heartBeat = (int) measurement.getAverageHeartBeat();
            mBpm.setText("" + heartBeat);
            mTimestamp.setText(DateUtil.formatDateTime(new Date(measurement.getStartMeasurement())));
            updateCurrentActivity();
            mBpm.setVisibility(View.VISIBLE);
            mBpmLabelContainer.setVisibility(VISIBLE);
            mTimestamp.setVisibility(VISIBLE);
            mCurrentActivity.setVisibility(VISIBLE);
            mAction.setVisibility(VISIBLE);
        }
    }

    private void updateCurrentActivity() {
        String currentActivity =
                Measurement.getActivityName(getContext(), WearUserPreferences.getInstance().getAcceptedActivity());
        mCurrentActivity.setText(getString(R.string.heart_rate_monitor_activity, currentActivity));
    }

    @Override
    public void onClick(View v) {
        updateCurrentActivity();
        if (v.getId() == R.id.action) {
            mBeating = mListener.toggleHeartRateMonitor();
            if (mBeating) {
                mTitle.setText(R.string.heart_rate_monitor_title_setup);
                mBpm.setText(R.string.heart_rate_monitor_empty_heart_beat);
                mAction.setText(R.string.heart_rate_monitor_action_stop);
                setMeasuringVisibility();
                mBeatingBpm = DEFAULT_HEART_BEATING_ANIMATION_BPM;
                playHeartBeat();
            }
        }
    }

    private void playHeartBeat() {
        if (mBeatingAnimation != null) {
            mBeatingAnimation.cancel();
            mBeatingAnimation = null;
        }

        long beatDuration = 60000 / mBeatingBpm;

        AnimatorSet animatorSet = new AnimatorSet();
        final ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(mHeartImage, "scaleX", 1.4f, 1f);
        final ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(mHeartImage, "scaleY", 1.4f, 1f);
        animatorSet.play(scaleXAnimator).with(scaleYAnimator);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(beatDuration);
        animatorSet.addListener(new AnimatorListener() {
            private boolean cancelled = false;

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelled && mBeating) {
                    playHeartBeat();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
        mBeatingAnimation = animatorSet;
    }
}
