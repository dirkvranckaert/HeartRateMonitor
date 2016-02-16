package eu.vranckaert.heart.rate.monitor.shared.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import eu.vranckaert.heart.rate.monitor.shared.R;

/**
 * Utility class for helping you in requesting permissions with displaying a rationale popup whenever needed and
 * handling success and denials through {@link Fragment#onRequestPermissionsResult(int, String[], int[])}.<br/>
 * Remember to not request for "NORMAL" permission. These permissions are considered safe to be automatically assigned
 * and are listed here: http://developer.android.com/guide/topics/security/normal-permissions.html.
 *
 * @author Dirk Vranckaert
 */
public class PermissionUtil {
    /**
     * Request a certain permission to the user. If the permission has already been granted nothing happens. However if
     * the permission is not yet (or not anymore) granted it will be decided if a permission rationale dialog (with some
     * more explanation about why you request the permission) should be shown or not, after which the permission will be
     * asked for, or if that rationale dialog is cancelled the permission will not be granted and
     * {@link Fragment#onRequestPermissionsResult(int, String[], int[])} will be called with a grant-result of
     * {@link PackageManager#PERMISSION_DENIED}.
     *
     * @param activity    The {@link Activity} from which you are asking for a permission.
     * @param requestCode The request code to ask for this permission. This is important to do any handling of a
     *                    permission denial or acceptance through the
     *                    {@link Fragment#onRequestPermissionsResult(int, String[], int[])} method.
     * @param permission  The permission you are requesting.
     * @param explanation An optionally explanation of why you would request this permission. If the user declines
     *                    the permission once and you ask for the permission again, this explanation or rationale
     *                    message will be shown to the user before he will be prompted again to grant the permission.
     * @return This method returns {@link Boolean#TRUE} if the permission you are requesting has already be granted or
     * {@link Boolean#FALSE} if the permission is not yet granted and the permission request dialog or the rationale
     * dialog in combination with the permission request dialog is shown or (if the user checked the "don't ask again"
     * checkbox) nothing will happen.
     */
    public static boolean requestPermission(final Activity activity, final int requestCode, final String permission,
                                            final String explanation) {
        if (VERSION.SDK_INT < VERSION_CODES.M) {
            return true;
        }

        if (!hasPermission(activity, permission)) {
            if (shouldShowRequestPermissionRationale(activity, permission) && !TextUtils.isEmpty(explanation)) {
                new AlertDialog.Builder(activity)
                        .setMessage(explanation)
                        .setPositiveButton(R.string.general_ok, new OnClickListener() {
                            @TargetApi(VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                activity.requestPermissions(new String[]{permission}, requestCode);
                            }
                        })
                        .setNegativeButton(R.string.general_cancel, new OnClickListener() {
                            @TargetApi(VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                activity.onRequestPermissionsResult(requestCode, new String[]{permission},
                                        new int[]{PackageManager.PERMISSION_DENIED});
                            }
                        })
                        .show();
            } else {
                activity.requestPermissions(new String[]{permission}, requestCode);
            }
            return false;
        }

        return true;
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        if (VERSION.SDK_INT < VERSION_CODES.M) {
            return false;
        }

        return activity.shouldShowRequestPermissionRationale(permission);
    }

    /**
     * Check if a certain permission has already be granted.
     *
     * @param context    The context.
     * @param permission The permission to check.
     * @return {@link Boolean#TRUE} if the permission has already be granted, {@link Boolean#FALSE} if not yet or not
     * anymore granted.
     */
    public static boolean hasPermission(Context context, String permission) {
        if (VERSION.SDK_INT < VERSION_CODES.M) {
            return true;
        }

        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }
}
