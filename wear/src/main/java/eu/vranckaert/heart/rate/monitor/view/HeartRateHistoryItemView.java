package eu.vranckaert.heart.rate.monitor.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.util.DateUtil;

import java.util.Date;
import java.util.Map;

/**
 * Date: 02/06/15
 * Time: 18:28
 *
 * @author Dirk Vranckaert
 */
public class HeartRateHistoryItemView extends AbstractViewHolder {
    private final TextView mBpm;
    private final TextView mDuration;
    private final TextView mDate;
    private final TextView mTime;

    int mOriginalPaddingTop = -1;
    int mOriginalPaddingBottom = -1;

    public HeartRateHistoryItemView(LayoutInflater inflater, ViewGroup parent) {
        super(inflater, parent, R.layout.heart_rate_history_item);

        mBpm = findViewById(R.id.bpm);
        mDuration = findViewById(R.id.duration);
        mDate = findViewById(R.id.date);
        mTime = findViewById(R.id.time);
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
    }

    public void setPaddingTop(int paddingTop) {
        if (mOriginalPaddingTop == -1) {
            mOriginalPaddingTop = itemView.getPaddingTop();
        }
        int paddingLeft = itemView.getPaddingLeft();
        int paddingRight = itemView.getPaddingRight();
        int paddingBottom = itemView.getPaddingBottom();
        int newPaddingTop = paddingTop == 0 ? mOriginalPaddingTop : paddingTop + mOriginalPaddingTop;

        itemView.setPadding(paddingLeft, newPaddingTop, paddingRight, paddingBottom);
    }

    public void setPaddingBottom(int paddingBottom) {
        if (mOriginalPaddingBottom == -1) {
            mOriginalPaddingBottom = itemView.getPaddingBottom();
        }
        int paddingLeft = itemView.getPaddingLeft();
        int paddingRight = itemView.getPaddingRight();
        int newPaddingBottom = paddingBottom == 0 ? mOriginalPaddingBottom : paddingBottom + mOriginalPaddingBottom;
        int paddingTop = itemView.getPaddingTop();

        itemView.setPadding(paddingLeft, paddingTop, paddingRight, newPaddingBottom);
    }
}
