package eu.vranckaert.heart.rate.monitor.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.view.HeartRateView.HeartRateListener;

/**
 * Date: 18/02/16
 * Time: 08:47
 *
 * @author Dirk Vranckaert
 */
public class HeartRateSettingsView extends AbstractViewHolder implements OnClickListener {
    private final HeartRateListener mListener;

    public HeartRateSettingsView(LayoutInflater inflater, ViewGroup parent, HeartRateListener listener) {
        super(inflater, parent, R.layout.heart_rate_settings);
        mListener = listener;

        getView().setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mListener.openSettings();
    }
}
