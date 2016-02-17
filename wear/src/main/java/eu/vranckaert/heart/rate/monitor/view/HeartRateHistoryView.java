package eu.vranckaert.heart.rate.monitor.view;

import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.util.BoxInsetLayoutUtil;
import eu.vranckaert.heart.rate.monitor.view.HeartRateView.HeartRateListener;
import eu.vranckaert.heart.rate.monitor.widget.GreedyRecyclerView;

import java.util.List;

/**
 * Date: 29/05/15
 * Time: 08:02
 *
 * @author Dirk Vranckaert
 */
public class HeartRateHistoryView extends AbstractViewHolder {
    private final HeartRateListener mListener;

    private final TextView mEmpty;
    private final GreedyRecyclerView mList;
    private final HeartRateHistoryAdapter mAdapter;
    private final LinearLayoutManager mLayoutManager;

    public HeartRateHistoryView(LayoutInflater inflater, ViewGroup parent, HeartRateListener listener) {
        super(inflater, parent, R.layout.heart_rate_history);
        mListener = listener;
        BoxInsetLayoutUtil.setReferenceBoxInsetLayoutView(getView(), true, true, false, false, listener.getBoxInsetReferenceView());

        mEmpty = findViewById(R.id.empty);
        mList = findViewById(R.id.list);
        mLayoutManager = new LinearLayoutManager(getContext());
        mList.setLayoutManager(mLayoutManager);
        mAdapter = new HeartRateHistoryAdapter(getContext(), listener);
        mList.setAdapter(mAdapter);

        mListener.getBoxInsetReferenceView().getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mAdapter.setMargins(mListener.getBoxInsetReferenceView().getPaddingTop(), mListener.getBoxInsetReferenceView().getPaddingBottom());
                        mListener.getBoxInsetReferenceView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        setMeasurements(null);
    }

    public void setMeasurements(List<Measurement> measurements) {
        boolean hasMeasurements = measurements != null && !measurements.isEmpty();
        mEmpty.setVisibility(hasMeasurements ? GONE : VISIBLE);
        mList.setVisibility(hasMeasurements ? VISIBLE : GONE);

        if (hasMeasurements) {
            mAdapter.setMeasurements(measurements);
        }
    }
}
