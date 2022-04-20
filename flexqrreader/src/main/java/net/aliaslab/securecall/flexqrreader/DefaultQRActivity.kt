package net.aliaslab.securecall.flexqrreader

import android.content.Intent
import android.util.Log

@Deprecated("This class has been deprecated. Use a subclass of AdaptiveScanningActivity instead.", replaceWith = ReplaceWith("AdaptiveScanningActivity"))
public class DefaultQRActivity: ScanQrActivity() {

    override fun handleEncodedData(encodedData: String) {
        Log.d("QRLauncher", "Scanner found result: $encodedData")
        val result = Intent()
        result.putExtra(QR_STRING_CONTENT_KEY, encodedData) // test
        setResult(RESULT_OK, result)
        finish()
    }

    companion object {
        @Deprecated("Will be deleted in the next major. Use AdaptiveScanningActivity.QR_STRING_CONTENT_KEY instead.", replaceWith = ReplaceWith("AdaptiveScanningActivity.QR_STRING_CONTENT_KEY"))
        const val QR_STRING_CONTENT_KEY = "qr_s"
    }
}