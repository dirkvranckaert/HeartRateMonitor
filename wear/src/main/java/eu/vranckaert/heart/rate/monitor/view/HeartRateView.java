package eu.vranckaert.heart.rate.monitor.view;

import android.content.Context;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import eu.vranckaert.heart.rate.monitor.R;

/**
 * Date: 28/05/15
 * Time: 15:51
 *
 * @author Dirk Vranckaert
 */
public class HeartRateView extends AbstractViewHolder {
    private final View mBoxInsetReference;
    private final HeartRateListener mListener;
    private GridViewPager mViewPager;
    private DotsPageIndicator mPageIndicator;

    public HeartRateView(Context context, HeartRateListener listener) {
        super(context, R.layout.heart_rate);
        mListener = listener;

        mBoxInsetReference = findViewById(R.id.boxinset_reference);
        mBoxInsetReference.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mViewPager = findViewById(R.id.view_pager);
                mViewPager.setAdapter(new HeartRateAdapter(getContext(), mListener));
                mPageIndicator = findViewById(R.id.page_indicator);
                mPageIndicator.setPager(mViewPager);
                mBoxInsetReference.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public View getBoxInsetReferenceView() {
        return mBoxInsetReference;
    }

    public interface HeartRateListener {
        void onHearRateViewCreated(AbstractViewHolder view);
        View getBoxInsetReferenceView();
        boolean toggleHeartRateMonitor();
    }
}
