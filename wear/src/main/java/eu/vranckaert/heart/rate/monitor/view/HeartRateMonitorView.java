package eu.vranckaert.heart.rate.monitor.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.WearUserPreferences;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.shared.util.DateUtil;
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
    private static final int MINIMUM_BPM = 50;

    private final HeartRateListener mListener;
    private final TextView mTimeLabel;
    private final TextView mTitle;
    private final ImageView mHeartImage;
    private final TextView mBpm;
    private final TextView mBpmLabel;
    private final View mBpmLabelContainer;
    private final TextView mTimestamp;
    private final Button mAction;

    private Measurement mLastMeasurement;
    private Animator mBeatingAnimation;
    private boolean mMeasuring = false;
    private boolean mFirstMeasurementDetected = false;
    private boolean mBeating = false;
    private boolean mAmbientMode = false;
    private Long mPrevMeasuringTime = null;
    private int mBeatingBpm;
    private boolean mStartedFollowingHeartBeat;

    public HeartRateMonitorView(LayoutInflater inflater, ViewGroup parent, HeartRateListener listener) {
        super(inflater, parent, R.layout.heart_rate_monitor);
        mListener = listener;
        BoxInsetLayoutUtil.setReferenceBoxInsetLayoutView(getView(), true, true, false, false,
                listener.getBoxInsetReferenceView());

        mTimeLabel = findViewById(R.id.time_label);
        mTitle = findViewById(R.id.title);
        mHeartImage = findViewById(R.id.heart_rate_img);
        mBpm = findViewById(R.id.heart_rate);
        mBpmLabel = findViewById(R.id.bpm_label);
        mBpmLabelContainer = findViewById(R.id.bpm_label_container);
        mTimestamp = findViewById(R.id.timestamp);
        mAction = findViewById(R.id.action);

        mBpm.setText(R.string.heart_rate_monitor_empty_heart_beat);
        mAction.setText(R.string.heart_rate_monitor_action_start);

        mAction.setOnClickListener(this);

        updateUIForAmbientMode();
    }

    private void updateUIForAmbientMode() {
        mTitle.setTextColor(
                getResources().getColor(mAmbientMode ? R.color.ambient_text_color : R.color.non_ambient_text_color));
        mTitle.getPaint().setAntiAlias(!mAmbientMode);
        mTitle.setVisibility((mAmbientMode && !mMeasuring && mLastMeasurement == null) ? GONE : VISIBLE);

        mHeartImage.setImageTintList(getResources()
                .getColorStateList(mAmbientMode ? R.color.ambient_text_color : R.color.non_ambient_text_color));

        mBpm.setTextColor(
                getResources().getColor(mAmbientMode ? R.color.ambient_text_color : R.color.non_ambient_text_color));
        mBpm.getPaint().setAntiAlias(!mAmbientMode);

        mBpmLabel.setTextColor(
                getResources().getColor(mAmbientMode ? R.color.ambient_text_color : R.color.non_ambient_text_color));
        mBpmLabel.getPaint().setAntiAlias(!mAmbientMode);

        mTimestamp.setTextColor(
                getResources().getColor(mAmbientMode ? R.color.ambient_text_color : R.color.non_ambient_text_color));
        mTimestamp.getPaint().setAntiAlias(!mAmbientMode);

        mAction.setVisibility(mAmbientMode ? GONE : VISIBLE);

        mTimeLabel.setVisibility(mAmbientMode ? VISIBLE : GONE);
        mTimeLabel.setText(DateUtil.formatTime(new Date()));
    }

    public void setMeasuringHeartBeat(int heartBeat) {
        mFirstMeasurementDetected = true;
        mBeatingBpm = heartBeat;

        long measuringTimeLapse = 0L;
        long ambientModeUiUpdateTimelapse = WearUserPreferences.getInstance().getAmbientModeUiUpdateTimeLapse();
        if (mAmbientMode) {
            if (mPrevMeasuringTime == null) {
                Log.d("dirk", "Setting prev measure time");
                mPrevMeasuringTime = new Date().getTime() -
                        ambientModeUiUpdateTimelapse;
            }

            long currentTime = new Date().getTime();
            measuringTimeLapse =  currentTime - mPrevMeasuringTime;
            Log.d("dirk", "measuringTimeLapse=" + measuringTimeLapse);
        }

        if (!mAmbientMode || measuringTimeLapse >= ambientModeUiUpdateTimelapse) {
            updateHeartRateMonitorView();
            Log.d("dirk", "Setting prev measure time");
            mPrevMeasuringTime = new Date().getTime();
        }
    }

    private void updateHeartRateMonitorView() {
        mTitle.setText(mMeasuring ? mFirstMeasurementDetected ? R.string.heart_rate_monitor_title_measuring :
                R.string.heart_rate_monitor_title_setup :
                mLastMeasurement == null ? R.string.heart_rate_monitor_title_no_history :
                        R.string.heart_rate_monitor_title_recent);
        mBpm.setText(mMeasuring ?
                mFirstMeasurementDetected ? "" + mBeatingBpm : getString(R.string.heart_rate_monitor_empty_heart_beat) :
                "" + (int) mLastMeasurement.getAverageHeartBeat());
        mAction.setText(
                mMeasuring ? R.string.heart_rate_monitor_action_stop : R.string.heart_rate_monitor_action_start);
        mTimestamp.setText(mLastMeasurement == null ? "" : DateUtil.formatDateTime(new Date(mLastMeasurement.getStartMeasurement())));
        updateVisibilities();
    }

    private void updateVisibilities() {
        mBpm.setVisibility(mMeasuring || mLastMeasurement != null ? VISIBLE : GONE);
        mBpmLabelContainer.setVisibility(mLastMeasurement == null ? GONE : VISIBLE);
        mTimestamp.setVisibility(mMeasuring || mLastMeasurement == null ? INVISIBLE : VISIBLE);
        mAction.setVisibility(mAmbientMode ? GONE : VISIBLE);

    }

    public void setLatestMeasurement(Measurement measurement) {
        mLastMeasurement = measurement;
        updateHeartRateMonitorView();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action) {
            mStartedFollowingHeartBeat = false;
            mMeasuring = mListener.toggleHeartRateMonitor();
            mFirstMeasurementDetected = false;
            mBeating = mMeasuring;
            updateHeartRateMonitorView();
            if (mMeasuring) {
                playHeartBeat();
            }
        }
    }

    private void toggleFollowingHeartBeat(boolean started) {
        if (!mStartedFollowingHeartBeat && mMeasuring) {
            return;
        }

        mAction.setEnabled(!started);
        mStartedFollowingHeartBeat = started;
        mMeasuring = started;
        mFirstMeasurementDetected = false;
        mBeating = started;
        updateHeartRateMonitorView();
        if (mMeasuring && !mAmbientMode) {
            playHeartBeat();
        }
    }

    public void startFollowingHeartBeat() {
        toggleFollowingHeartBeat(true);
    }

    public void stopFollowingHeartBeat() {
        toggleFollowingHeartBeat(false);
    }

    public void followingHeartBeat(int bpm) {
        if (!mStartedFollowingHeartBeat && mMeasuring) {
            return;
        }

        if (!mStartedFollowingHeartBeat) {
            startFollowingHeartBeat();
        }
        setMeasuringHeartBeat(bpm);
    }

    private void playHeartBeat() {
        if (mBeatingAnimation != null) {
            mBeatingAnimation.cancel();
            mBeatingAnimation = null;
        }

        int bpm = mBeatingBpm;
        if (bpm == 0) {
            bpm = MINIMUM_BPM;
        }

        long beatDuration = 60000 / bpm;

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
                if (!cancelled && mBeating && !mAmbientMode) {
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

    public void startAmbientMode() {
        mAmbientMode = true;
        updateTime();
        updateUIForAmbientMode();
    }

    public void stopAmbientMode() {
        mAmbientMode = false;
        updateUIForAmbientMode();
        if (mBeating && mFirstMeasurementDetected) {
            playHeartBeat();
        }
    }

    public void updateInAmbient() {
        updateTime();
        updateHeartRateMonitorView();
    }

    private void updateTime() {
        mTimeLabel.setText(DateUtil.formatTime(new Date()));
    }
}
