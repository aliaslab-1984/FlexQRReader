package net.aliaslab.securecall.flexqrreader.playvision

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.mlkit.vision.barcode.common.Barcode
import net.aliaslab.securecall.flexqrreader.playvision.camerax.QRScanningViewModel
import java.lang.Exception

public abstract class QRScannerActivity: AppCompatActivity() {

    private val viewModel: QRScanningViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val observer = Observer<List<Barcode>> { barcodeList ->
            // React when the property becomes true
            if (barcodeList.isNotEmpty()) {

                viewModel.strings.value = barcodeList.mapNotNull {
                    it.rawValue
                }
            }
        }

        val stringsObserver = Observer<List<String>> { stringList ->
            // React when the property becomes true
            if (stringList.isNotEmpty()) {
                val firstValue = stringList.first()
                handleResult(firstValue)
            }
        }

        viewModel.strings.observe(this, stringsObserver)
        viewModel.barcodes.observe(this, observer)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.barcodes.removeObservers(this)
    }

    public fun handleResult(stringResult: String) {
        // handle the result depending on the request
        // Better to hide the result, just logging it's length
        Log.d("QRScannerActivity","Scanner found result: it's ${stringResult.length} long.")
        val result = Intent()
        result.putExtra(QrCodeScanActivity.QR_STRING_CONTENT_KEY, stringResult) // test
        setResult(RESULT_OK, result)
        finish()
    }

    companion object {
        public const val QR_STRING_CONTENT_KEY = "qr_s"
    }

}