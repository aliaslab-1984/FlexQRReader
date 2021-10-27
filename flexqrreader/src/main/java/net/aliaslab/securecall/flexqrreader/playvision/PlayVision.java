package net.aliaslab.securecall.flexqrreader.playvision;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.barcode.BarcodeDetector;


public class PlayVision {

    private static final String TAG = "PlayVision";

    private PlayVision() { /* */ }

    public static boolean checkPlayServices(final Activity activity) {

        if (activity == null || activity.isFinishing()) {
            return false;
        }

        //boolean playServicesAvailable = isGooglePlayServicesAvailable(activity)

        final boolean isOperational = isMobileVisionOperational(activity);
        if (!isOperational) {
            Log.w(TAG, "play_services_not_operational");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = activity.registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Log.w(TAG, "play_services_low_storage_error");
            }
        }
        return isOperational;
    }

    /**
     * Allows to statically check if Google Play Services are available on the system.
     * If unavailable for user-solvable reasons, this also prompts the user for resolution.
     * @param activity The activity
     * @return True if Google Play Services are available, false otherwise
     */
    private static boolean isGooglePlayServicesAvailable(@NonNull final Activity activity) {

        final GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    /**
     * Allows to statically check if Google Play Services' Mobile Vision Module is available on the system.
     * @param context The context
     * @return True if barcodeDetector is operational, false otherwise
     */
    private static boolean isMobileVisionOperational(@NonNull final Context context) {

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context)
                .build();
        return barcodeDetector.isOperational();
    }
}
