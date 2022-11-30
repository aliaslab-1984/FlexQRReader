package net.aliaslab.securecall.flexqrreader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.mlkit.vision.barcode.common.Barcode
import net.aliaslab.securecall.flexqrreader.playvision.PlayVision
import net.aliaslab.securecall.flexqrreader.playvision.camerax.QRScanningViewModel
import net.aliaslab.securecall.flexqrreader.utils.QrUtils
import net.aliaslab.securecall.flexqrreader.utils.Utils

abstract class AdaptiveScanningActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_REQUEST_CAMERA = 1

    private var checkingPlayServices = false
    var isScanningQR = false

    private val viewModel: QRScanningViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // We set the content view into the scanQR() method (which is called by readQR()).

        observeViewModel()
        readQr()
    }

    override fun onResume() {
        super.onResume()

        isScanningQR = false
        checkingPlayServices = false
    }

    /**
     * Returns the fragment that will be used to scan a barcode/qrcode by using the Google Play Services API.
     */
    open abstract fun googlePlayServicesFragment(): Fragment

    /**
     * Returns the fragment that will be used to scan a barcode/qrcode by using the Zebra API.
     */
    open abstract fun zebraFragment(): Fragment

    private fun readQr() {

        if (checkingPlayServices) {     // avoid double ScanActivity
            return
        }
        checkingPlayServices = true

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
            return
        }
        scanQr()
    }

    public fun getCorrectFragment(): Fragment {
        val useZx = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getBoolean("zx", false)
        // val useCameraX = PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("camerax", true)
        val verboseMsg = "Starting QRcodeScanner..."

        Utils.getPackageInfo(this)

        return if (!useZx && QrUtils.isHms(this) /*&& PlayVision.isHmsAvailable(getApplicationContext())*/) {
            // TODO: Huawei
            Log.d("RAD42H", verboseMsg)
            // ReadingQrManager.startQrHms(this, REQUEST_CODE_DEFINE)
            zebraFragment()
        } else if (!useZx && PlayVision.checkPlayServices(this)) {
            googlePlayServicesFragment()
        } else {
            zebraFragment()
        }
    }

    /**
     * This method is used to specify which layout should be used to display this fragment.
     * Check [scanQr][DefaultAdaptiveScanningActivity.scanQr] for a standard implementation.
     */
    open abstract fun scanQr()

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String?>,
                                            grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanQr()
            } else {
                // permission denied, boo! Disable the functionality that depends on this permission.
                setResult(RESULT_CANCELED)
            }
        }
    }

    private val observer = Observer<List<Barcode>> { barcodeList ->
        // React when the property becomes true
        if (barcodeList.isNotEmpty()) {

            viewModel.strings.value = barcodeList.mapNotNull {
                it.rawValue
            }
        }
    }

    private val stringsObserver = Observer<List<String>> { stringList ->
        // React when the property becomes true
        if (stringList.isNotEmpty()) {
            val firstValue = stringList.first()
            handleEncodedData(firstValue)
        }
    }

    /**
     * Observes every scan result that gets pushed.
     * As soon as we find a valid result, we handle it and we quit scanning.
     */
    private fun observeViewModel() {

        viewModel.strings.observe(this, stringsObserver)
        viewModel.barcodes.observe(this, observer)
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.strings.removeObserver(stringsObserver)
        viewModel.barcodes.removeObserver(observer)
    }

    /**
     * This method gets called when the child QR Scanner Activity finds a valid string from the QR Code.
     * @param encodedData the data that has been extracted from the QR Code.
     *
     * Here you can process the extracted data.
     */
    private fun handleEncodedData(encodedData: String) {
        Log.d("QRLauncher", "Scanner found result: $encodedData")
        val result = Intent()
        result.putExtra(QR_STRING_CONTENT_KEY, encodedData) // test
        setResult(RESULT_OK, result)
        finish()
    }

    companion object {
        /**
         * The key-string used to store into the intent result the scanned text.
         */
        public const val QR_STRING_CONTENT_KEY = "qr_s"
    }

}