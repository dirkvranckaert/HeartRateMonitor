package eu.vranckaert.heart.rate.monitor.view;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import eu.vranckaert.heart.rate.monitor.R;

/**
 * Date: 12/04/16
 * Time: 13:28
 *
 * @author Dirk Vranckaert
 */
public class HeartRateBodySensorPermissionDenied extends AbstractViewHolder implements OnClickListener {
    private final HeartRateBodySensorPermissionDeniedListener mListener;

    public HeartRateBodySensorPermissionDenied(Context context, HeartRateBodySensorPermissionDeniedListener listener, boolean canRetry) {
        super(context, R.layout.heart_rate_body_sensor_permission_denied);
        mListener = listener;

        TextView message = findViewById(R.id.message);
        View grantPermission = findViewById(R.id.grant);
        if (canRetry) {
            grantPermission.setOnClickListener(this);
        } else {
            grantPermission.setVisibility(GONE);
            message.setText(R.string.heart_rate_body_sensor_permission_denied_open_settings);
        }
    }

    @Override
    public void onClick(View v) {
        mListener.grantBodySensorPermission();
    }

    public interface HeartRateBodySensorPermissionDeniedListener {
        void grantBodySensorPermission();
    }
}
