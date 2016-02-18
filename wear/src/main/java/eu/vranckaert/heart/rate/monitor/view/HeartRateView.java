package eu.vranckaert.heart.rate.monitor.view;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;

/**
 * Date: 28/05/15
 * Time: 15:51
 *
 * @author Dirk Vranckaert
 */
public class HeartRateView extends AbstractViewHolder {
    private final View mBoxInsetReference;
    private final HeartRateListener mListener;
    private HeartRateAdapter mAdapter;
    private GridViewPager mViewPager;
    private DotsPageIndicator mPageIndicator;

    public HeartRateView(Context context, HeartRateListener listener) {
        super(context, R.layout.heart_rate);
        mListener = listener;

        mBoxInsetReference = findViewById(R.id.boxinset_reference);
        mBoxInsetReference.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mAdapter = new HeartRateAdapter(getContext(), mListener);
                mViewPager = findViewById(R.id.view_pager);
                mViewPager.setAdapter(mAdapter);
                mPageIndicator = findViewById(R.id.page_indicator);
                mPageIndicator.setPager(mViewPager);
                mBoxInsetReference.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public View getBoxInsetReferenceView() {
        return mBoxInsetReference;
    }

    public void startAmbientMode() {
        mViewPager.setCurrentItem(0, 0, true);
        mViewPager.setEnabled(false);
        mAdapter.startAmbientMode();
        getView().setBackgroundColor(getResources().getColor(R.color.ambient_bg_color));
    }

    public void stopAmbientMode() {
        mViewPager.setEnabled(false);
        mAdapter.stopAmbientMode();
        getView().setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    public void updateInAmbient() {
        mAdapter.updateInAmbient();
    }

    public interface HeartRateListener {
        void onHearRateViewCreated(AbstractViewHolder view);

        View getBoxInsetReferenceView();

        boolean toggleHeartRateMonitor();

        void openSettings();

        void onItemSelected(Measurement measurement);
    }
}
