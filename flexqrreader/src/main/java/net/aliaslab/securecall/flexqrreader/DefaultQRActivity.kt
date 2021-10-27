package net.aliaslab.securecall.flexqrreader

import android.content.Intent
import android.util.Log

public class DefaultQRActivity: ScanQrActivity() {

    override fun handleEncodedData(encodedData: String) {
        Log.d("QRLauncher", "Scanner found result: $encodedData")
        val result = Intent()
        result.putExtra(QR_STRING_CONTENT_KEY, encodedData) // test
        setResult(RESULT_OK, result)
        finish()
    }

    companion object {
        const val QR_STRING_CONTENT_KEY = "qr_s"
    }
}