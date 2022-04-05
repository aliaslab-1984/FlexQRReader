package net.aliaslab.securecall.flexqrreader

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import net.aliaslab.securecall.flexqrreader.utils.QrUtils
import net.aliaslab.securecall.flexqrreader.R
import net.aliaslab.securecall.flexqrreader.playvision.PlayVision
import net.aliaslab.securecall.flexqrreader.utils.Utils
import net.aliaslab.securecall.flexqrreader.playvision.QrCodeScanActivity
import net.aliaslab.securecall.flexqrreader.playvision.camerax.QRScanActivityX
import net.aliaslab.securecall.flexqrreader.zxing.IntentIntegrator
import net.aliaslab.securecall.flexqrreader.zxing.IntentResult
import net.aliaslab.securecall.flexqrreader.zxing.android.Intents
import java.util.ArrayList

/**
 * Abstract class that defines the logic used to show a QR Scanner View.
 * It fetches the user preferences, anc checks the existence, and the value, of the *"zx"* property.
 * If the preference is set to true, it uses the ZXing activity to scan the QR Code. If it's set to false,
 * it checks if the Google Play services are installed on the device, if the check succeeds, it uses the GooglePlay scanner, otherwise it
 * uses the ZXing activity as a backup.
 *
 * ## Subclassing
 * One important part of the class is creating subclasses, that handle the parsing of the QR String differently.
 * There's only one method that needs to be overridden: `handleEncodedData(encodedData: String)`
 * This method will be called every time the child QR Activity reads a string from the QR code.
 */
public abstract class ScanQrActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_REQUEST_CAMERA = 1
    private val QR_DECODING = 2

    private var checkingPlayServices = false
    private var isScanningQR = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scan)

        readQr()
    }

    override fun onResume() {
        super.onResume()

        isScanningQR = false
        checkingPlayServices = false
    }

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

    private fun scanQr() {

        val useZx = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getBoolean("zx", false)
        val useCameraX = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getBoolean("camerax", false)
        val verboseMsg = "Starting QRcodeScanner..."

        Utils.getPackageInfo(this)

        if (!useZx && QrUtils.isHms(this) /*&& PlayVision.isHmsAvailable(getApplicationContext())*/) {

            // TODO: Huawei
            Log.d("RAD42H", verboseMsg)
            // ReadingQrManager.startQrHms(this, REQUEST_CODE_DEFINE)
        } else if (!useZx && PlayVision.checkPlayServices(this)) {

            // Google Play services
                // LogEngine.debug("RAD42G", verboseMsg)
                    if (useCameraX) {
                        val intent = Intent(this, QRScanActivityX::class.java)
                        resultLauncher.launch(intent)
                    } else {
                        val intent = Intent(this, QrCodeScanActivity::class.java)
                        resultLauncher.launch(intent)
                    }
        } else {

            // Zebra fallback
            Log.d("RAD42Z", verboseMsg)
            val intentIntegrator = IntentIntegrator(this)
            intentIntegrator.addExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS, 0L)
            val formats = ArrayList<String>()
            formats.add("QR_CODE")
            val alert = intentIntegrator.initiateScan(formats)
            alert?.show()
        }
        isScanningQR = true
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            manageQrIntent(result.data, "[PlayServices scanner]")
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int,
                                  data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val ir: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        manageQrData(ir?.contents, "[Zxing scanner]")
    }

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

    private fun manageQrIntent(data: Intent?, key: String) {

        val qrData = data?.extras?.getString(QrCodeScanActivity.QR_STRING_CONTENT_KEY)
        manageQrData(qrData, key)
    }

    private fun manageQrData(qrData: String?, key: String) {

        qrData?.let { handleEncodedData(it) }
            ?: Log.v("RAD403","enroll failed: decoding error [$key]")
    }

    /**
     * This method gets called when the child QR Scanner Activity finds a valid string from the QR Code.
     * @param encodedData the data that has been extracted from the QR Code.
     *
     * Here you can process the extracted data.
     */
    abstract fun handleEncodedData(encodedData: String)
}