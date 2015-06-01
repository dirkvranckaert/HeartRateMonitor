package eu.vranckaert.heart.rate.monitor.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import eu.vranckaert.heart.rate.monitor.Measurement;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.util.BoxInsetLayoutUtil;
import eu.vranckaert.heart.rate.monitor.view.HeartRateView.HeartRateListener;

import java.util.List;

/**
 * Date: 29/05/15
 * Time: 08:02
 *
 * @author Dirk Vranckaert
 */
public class HearRateHistoryView extends AbstractViewHolder {
    private final HeartRateListener mListener;

    public HearRateHistoryView(LayoutInflater inflater, ViewGroup parent, HeartRateListener listener) {
        super(inflater, parent, R.layout.heart_rate_history);
        mListener = listener;
        BoxInsetLayoutUtil.setReferenceBoxInsetLayoutView(getView(), true, true, false, false, listener.getBoxInsetReferenceView());
    }

    public void setMeasurements(List<Measurement> measurements) {

    }
}
