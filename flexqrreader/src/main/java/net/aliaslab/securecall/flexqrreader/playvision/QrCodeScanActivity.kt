/*
 * Activity which can be called for result and handle the scanning of QRcodes and the decoding of
 * the encrypted result
 */
package net.aliaslab.securecall.flexqrreader.playvision

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import net.aliaslab.securecall.flexqrreader.R
import net.aliaslab.securecall.flexqrreader.playvision.camera.CameraSource
import net.aliaslab.securecall.flexqrreader.playvision.camera.CameraSourcePreview
import java.io.IOException

/**
 * Activity which provides a QR-Code scanner. Start this activity with startActivityForResult. When
 * a QR-code is found the activity finishes. To get the result, override onActivityResult in the caller
 * activity, and retrieve the string with key QrCodeScanActivity.QR_STRING_CONTENT_KEY from the extras.
 */
public class QrCodeScanActivity : Activity() {
    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var needsQR = true
    private var barcodeDetector: BarcodeDetector? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scan)
        preview = findViewById(R.id.preview)
        prepareQRScanner()
    }

    public override fun onResume() {
        super.onResume()
        startQRScanner()
    }

    public override fun onPause() {
        super.onPause()
        stopQRScanner()
    }

    private fun handleResult(stringResult: String) {
        // handle the result depending on the request
        // Better to hide the result, just logging it's length
        Log.d("QRCodeScanActivity","Scanner found result: it's ${stringResult.length} long.")
        val result = Intent()
        result.putExtra(QR_STRING_CONTENT_KEY, stringResult) // test
        setResult(RESULT_OK, result)
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED)
            finish()
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun prepareQRScanner() {
        val detector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()
        barcodeDetector = detector
        cameraSource = CameraSource.Builder(this, barcodeDetector)
            // .setRequestedPreviewSize(640, 480)
            .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
            .build()

        detector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() { /* */
            }

            override fun receiveDetections(detections: Detections<Barcode>) {
                val barcodes = detections.detectedItems
                val r = Runnable {
                    if (needsQR && barcodes.size() != 0) {
//                          Toast.makeText(getApplicationContext(),"found code", Toast.LENGTH_SHORT).show()
                        needsQR = false
                        // stopQRScanner()
                        handleResult(barcodes.valueAt(0).displayValue)
                    }
                }
                preview!!.post(r)
            }
        })

        /*    if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            } else {
                Toast.makeText(this, R.string.missing_detector_error, Toast.LENGTH_LONG).show();
            }

        }*/
    }

    private fun startQRScanner() {
        needsQR = true
        Log.v("QRCodeScanActivity", "QRCodeScanner starting...")
        try {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    MY_PERMISSIONS_REQUEST_CAMERA
                )
                return
            }
            preview!!.start(cameraSource)
        } catch (e: IOException) {
            Log.e("QRCodeScanActivity","Unable to start camera source" + e.localizedMessage)
            cameraSource!!.release()
            cameraSource = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                startQRScanner()
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Log.v("QRCodeScanActivity","cannot start camera, permission denied by user")
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun stopQRScanner() {
        Log.d("QRCodeScanActivity","QR-Code Scanner stopped")
        if (preview != null) {
            preview!!.stop()
        }
    }

    companion object {
        const val QR_STRING_CONTENT_KEY = "qr_s"
        private const val MY_PERMISSIONS_REQUEST_CAMERA = 1
    }
}