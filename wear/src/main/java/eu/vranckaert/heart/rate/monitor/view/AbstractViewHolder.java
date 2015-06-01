package eu.vranckaert.heart.rate.monitor.view;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.vranckaert.heart.rate.monitor.util.MetricHelper;

/**
 * Date: 26/03/14
 * Time: 07:32
 *
 * @author Dirk Vranckaert
 */
public abstract class AbstractViewHolder {
    protected static final int VISIBLE = View.VISIBLE;
    protected static final int GONE = View.GONE;
    protected static final int INVISIBLE = View.INVISIBLE;
    protected static final int OVER_SCROLL_NEVER = View.OVER_SCROLL_NEVER;
    protected final View view;

    /**
     * Default constructor
     *
     * @param context
     * @param layoutResource
     */
    public AbstractViewHolder(Context context, int layoutResource) {
        this(context, null, layoutResource);
    }

    /**
     * Constructor with possibility to add parent (to be used in listviews)
     *
     * @param context
     * @param parent
     * @param layoutResource
     */
    public AbstractViewHolder(Context context, ViewGroup parent,
                              int layoutResource) {
        this(LayoutInflater.from(context), parent, layoutResource);
    }

    public AbstractViewHolder(LayoutInflater inflater, ViewGroup parent,
                              int layoutResource) {
        view = inflater.inflate(layoutResource, parent, false);
        view.setTag(this);
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T findViewById(int id) {
        return (T) view.findViewById(id);
    }

    public Context getContext() {
        return view.getContext();
    }

    protected Resources getResources() {
        return view.getResources();
    }

    protected String getString(int id) {
        return getResources().getString(id);
    }

    protected String getString(int id, int size) {
        return getResources().getString(id, size);
    }

    protected String getString(int id, String item) {
        return getResources().getString(id, item);
    }

    protected int getColor(int id) {
        return getResources().getColor(id);
    }

    public View getView() {
        return view;
    }

    protected int toDp(int i) {
        return MetricHelper.pixelToDp(getContext(), i);
    }

    protected int toPx(int dp) {
        return MetricHelper.dpToPixel(getContext(), dp);
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractViewHolder> T fromView(View v) {
        if (v != null) {
            return (T) v.getTag();
        }
        return null;
    }

    public void post(Runnable runnable) {
        view.post(runnable);
    }

    protected void applyTextColorSpan(TextView view, String text, String search, int color) {
        int start = text.indexOf(search);
        if (start == -1) {
            view.setText(text);
            return;
        }
        text = text.replaceFirst(search, "");
        int end = text.indexOf(search);
        if (end == -1) {
            end = text.length();
        }
        SpannableString spannable = new SpannableString(text.replace(search, ""));
        spannable.setSpan(new ForegroundColorSpan(color), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(spannable);
    }
}
