package net.aliaslab.securecall.flexqrreader.playvision;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class PlayVision {

    private static final String TAG = "PlayVision";

    private PlayVision() { /* private constructor */ }

    /**
     * Checks if ML Kit is available (always true if dependency is included).
     * Also logs a warning if device has low storage (just like old Mobile Vision check).
     *
     * @param activity The activity context
     * @return true if ML Kit is ready
     */
    public static boolean checkMlKit(final Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return false;
        }

        final boolean isOperational = isMlKitAvailable();

        if (!isOperational) {
            Log.w(TAG, "mlkit_not_operational");

            // Optional: check for low storage
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = activity.registerReceiver(null, lowstorageFilter) != null;
            if (hasLowStorage) {
                Log.w(TAG, "mlkit_low_storage_warning");
            }
        }

        return isOperational;
    }

    /**
     * ML Kit is bundled via dependency, so it is always operational.
     */
    private static boolean isMlKitAvailable() {
        return true;
    }
}
