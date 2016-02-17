package eu.vranckaert.heart.rate.monitor.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.view.HeartRateView.HeartRateListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 02/06/15
 * Time: 18:22
 *
 * @author Dirk Vranckaert
 */
public class HeartRateHistoryAdapter extends Adapter {
    private final LayoutInflater mLayoutInflater;
    private final HeartRateListener mListener;
    private final List<Measurement> mMeasurements = new ArrayList<>();

    private int mPaddingTop;
    private int mPaddingBottom;

    public HeartRateHistoryAdapter(Context context, HeartRateListener listener) {
        mLayoutInflater = LayoutInflater.from(context);
        mListener = listener;
    }

    public void setMeasurements(List<Measurement> measurements) {
        mMeasurements.clear();
        mMeasurements.addAll(measurements);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HeartRateHistoryItemView(mLayoutInflater, parent, mListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Measurement measurement = getItemAt(position);
        HeartRateHistoryItemView historyItemView = (HeartRateHistoryItemView) holder;
        historyItemView.setMeasurement(measurement);

        if (position == 0) {
            historyItemView.setPaddingTop(mPaddingTop);
            historyItemView.setPaddingBottom(0);
        } else if (position == getItemCount() - 1) {
            historyItemView.setPaddingTop(0);
            historyItemView.setPaddingBottom(mPaddingBottom);
        } else {
            historyItemView.setPaddingTop(0);
            historyItemView.setPaddingBottom(0);
        }
    }

    public Measurement getItemAt(int position) {
        return mMeasurements.get(position);
    }

    @Override
    public int getItemCount() {
        return mMeasurements.size();
    }

    public void setMargins(int paddingTop, int paddingBottom) {
        mPaddingTop = paddingTop;
        mPaddingBottom = paddingBottom;
        notifyDataSetChanged();
    }
}
