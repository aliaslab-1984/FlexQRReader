package net.aliaslab.securecall.flexqrreader.playvision.camerax

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.commit
import com.google.mlkit.vision.barcode.common.Barcode
import net.aliaslab.securecall.flexqrreader.R
import net.aliaslab.securecall.flexqrreader.playvision.QrCodeScanActivity
import androidx.lifecycle.Observer

class QRScanActivityX : AppCompatActivity() {

    private val viewModel: QRScanningViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscan_x)

        val fragment = CameraScannerXFragment()
        supportFragmentManager.commit {
            replace(R.id.main_frame_layout, fragment)
            setPrimaryNavigationFragment(fragment)
        }

        val observer = Observer<List<Barcode>> { barcodeList ->
            // React when the property becomes true
            if (barcodeList.isNotEmpty()) {
                val firstValue = barcodeList.first().rawValue
                if (firstValue != null) {
                    handleResult(firstValue)
                }
            }
        }

        viewModel.barcodes.observe(this, observer)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.barcodes.removeObservers(this)
    }

    private fun handleResult(stringResult: String) {
        // handle the result depending on the request
        // Better to hide the result, just logging it's length
        Log.d("QRScanActivityX","Scanner found result: it's ${stringResult.length} long.")
        val result = Intent()
        result.putExtra(QrCodeScanActivity.QR_STRING_CONTENT_KEY, stringResult) // test
        setResult(RESULT_OK, result)
        finish()
    }
}