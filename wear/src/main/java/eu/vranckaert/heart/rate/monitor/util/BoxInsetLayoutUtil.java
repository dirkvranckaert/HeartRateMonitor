package eu.vranckaert.heart.rate.monitor.util;

import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView;

/**
 * Date: 29/05/15
 * Time: 10:35
 *
 * @author Dirk Vranckaert
 */
public class BoxInsetLayoutUtil {
    /**
     * Fixes the BoxInsetLayout shortcomings explained here: http://stackoverflow.com/a/30373269/745224
     */
    public static boolean setReferenceBoxInsetLayoutView(View boxedView, boolean left, boolean right, boolean top,
                                                         boolean bottom, View boxInsetLayoutReferenceView) {
        int inboxOffsetLeft = boxInsetLayoutReferenceView.getPaddingLeft();
        int inboxOffsetRight = boxInsetLayoutReferenceView.getPaddingRight();
        int inboxOffsetTop = boxInsetLayoutReferenceView.getPaddingTop();
        int inboxOffsetBottom = boxInsetLayoutReferenceView.getPaddingBottom();

        if (boxedView.getLayoutParams() instanceof AbsListView.LayoutParams) {
            boxedView.setPadding(left ? boxedView.getPaddingLeft() + inboxOffsetLeft : 0,
                    top ? boxedView.getPaddingTop() + inboxOffsetTop : 0,
                    right ? boxedView.getPaddingRight() + inboxOffsetRight : 0,
                    bottom ? boxedView.getPaddingBottom() + inboxOffsetBottom : 0);
        } else {
            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) boxedView.getLayoutParams();
            if (left) {
                marginLayoutParams.leftMargin += inboxOffsetLeft;
            }
            if (right) {
                marginLayoutParams.rightMargin += inboxOffsetRight;
            }
            if (top) {
                marginLayoutParams.topMargin += inboxOffsetTop;
            }
            if (bottom) {
                marginLayoutParams.bottomMargin += inboxOffsetBottom;
            }
            boxedView.setLayoutParams(marginLayoutParams);
        }

        return ((BoxInsetLayout) boxInsetLayoutReferenceView.getParent()).isRound();
    }
}
