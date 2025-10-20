package net.aliaslab.securecall.flexqrreader

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.journeyapps.barcodescanner.ScanOptions
import net.aliaslab.securecall.flexqrreader.playvision.QrCodeScanActivity
import net.aliaslab.securecall.flexqrreader.utils.Utils
import net.aliaslab.securecall.flexqrreader.zxing.IntentIntegrator
import net.aliaslab.securecall.flexqrreader.zxing.IntentResult
import net.aliaslab.securecall.flexqrreader.zxing.ZXScanActivity
import java.util.concurrent.Executors

@Deprecated("This class has been deprecated.", ReplaceWith("AdaptiveScanningActivity"))
abstract class ScanQrActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_REQUEST_CAMERA = 1
    private var isScanningQR = false

    private lateinit var previewView: PreviewView
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val scanner = BarcodeScanning.getClient()

    open lateinit var customZXIntent: ScanOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previewView = PreviewView(this)
        setContentView(previewView)

        setupIntent()
        readQr()
    }

    open fun setupIntent() {
        customZXIntent = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setCaptureActivity(ZXScanActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        isScanningQR = false
    }

    private fun readQr() {
        if (isScanningQR) return

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                MY_PERMISSIONS_REQUEST_CAMERA
            )
            return
        }

        scanQr()
    }
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun scanQr() {
        val useZx = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getBoolean("zx", true)
        val useCameraX = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getBoolean("camerax", true)

        Utils.getPackageInfo(this)

        when {
            !useZx -> {
                // Use ML Kit + CameraX
                if (useCameraX) {
                    startCameraX()
                } else {
                    // Fallback: could still use legacy PlayVision QR activity
                    val intent = Intent(this, QrCodeScanActivity::class.java)
                    resultLauncher.launch(intent)
                }
            }
            else -> {
                // ZXing fallback
                resultLauncher.launch(customZXIntent.createScanIntent(this))
            }
        }

        isScanningQR = true
    }

    private fun startCameraX() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis)

        }, ContextCompat.getMainExecutor(this))
    }
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let {
                            handleEncodedData(it)
                            finish() // close activity once a QR code is found
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ScanQrActivity", "ML Kit scan error: ${e.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                manageQrIntent(result.data, "[PlayServices / legacy scanner]")
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val ir: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        manageQrData(ir?.contents, "[Zxing scanner]")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanQr()
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun manageQrIntent(data: Intent?, key: String) {
        val qrData = data?.extras?.getString(QrCodeScanActivity.QR_STRING_CONTENT_KEY)
        manageQrData(qrData, key)
    }

    private fun manageQrData(qrData: String?, key: String) {
        qrData?.let { handleEncodedData(it) }
            ?: Log.v("ScanQrActivity", "Decoding error [$key]")
    }

    /**
     * Called when a QR code is successfully decoded
     */
    abstract fun handleEncodedData(encodedData: String)
}
