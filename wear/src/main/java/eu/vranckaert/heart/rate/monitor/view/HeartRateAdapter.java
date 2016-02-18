package eu.vranckaert.heart.rate.monitor.view;

import android.content.Context;
import android.support.wearable.view.GridPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.vranckaert.heart.rate.monitor.view.HeartRateView.HeartRateListener;

/**
 * Date: 29/05/15
 * Time: 07:58
 *
 * @author Dirk Vranckaert
 */
public class HeartRateAdapter extends GridPagerAdapter {
    private final LayoutInflater mLayoutInflater;
    private final HeartRateListener mListener;

    private HeartRateMonitorView mMonitorView;
    private HeartRateHistoryView mHistoryView;
    private HeartRateSettingsView mSettingsView;

    public HeartRateAdapter(Context context, HeartRateListener listener) {
        mLayoutInflater = LayoutInflater.from(context);
        mListener = listener;
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int i) {
        return 3;
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int row, int column) {
        AbstractViewHolder view = null;

        if (column == 0) {
            if (mMonitorView == null) {
                mMonitorView = new HeartRateMonitorView(mLayoutInflater, viewGroup, mListener);
            }
            view = mMonitorView;
        } else if (column == 1) {
            if (mHistoryView == null) {
                mHistoryView = new HeartRateHistoryView(mLayoutInflater, viewGroup, mListener);
            }
            view = mHistoryView;
        } else if (column == 2) {
            if (mSettingsView == null) {
                mSettingsView = new HeartRateSettingsView(mLayoutInflater, viewGroup, mListener);
            }
            view = mSettingsView;
        }

        if (view != null) {
            mListener.onHearRateViewCreated(view);
            viewGroup.addView(view.getView());
        }
        return view;
    }

    @Override
    public void destroyItem(ViewGroup viewGroup, int row, int column, Object o) {
        if (o instanceof AbstractViewHolder) {
            viewGroup.removeView(((AbstractViewHolder) o).getView());
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        if (o instanceof AbstractViewHolder) {
            return ((AbstractViewHolder) o).getView().equals(view);
        }

        return false;
    }
}
