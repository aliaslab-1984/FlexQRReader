package net.aliaslab.securecall.flexqrreader.playvision

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import net.aliaslab.securecall.flexqrreader.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExperimentalGetImage class QrCodeScanActivity : Activity() {

    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private var needsQR = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scan)

        previewView = findViewById(R.id.preview)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onResume() {
        super.onResume()
        needsQR = true
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun handleResult(stringResult: String) {
        Log.d("QRCodeScanActivity", "Scanner found result: it's ${stringResult.length} long.")
        val result = Intent()
        result.putExtra(QR_STRING_CONTENT_KEY, stringResult)
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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val barcodeScanner = BarcodeScanning.getClient()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(barcodeScanner, imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("QRCodeScanActivity", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageProxy(scanner: com.google.mlkit.vision.barcode.BarcodeScanner, imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (needsQR) {
                        for (barcode in barcodes) {
                            if (barcode.valueType == Barcode.TYPE_TEXT || barcode.valueType == Barcode.TYPE_URL) {
                                needsQR = false
                                handleResult(barcode.rawValue ?: "")
                                break
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("QRCodeScanActivity", "Scan failed: ${it.localizedMessage}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Log.v("QRCodeScanActivity", "cannot start camera, permission denied by user")
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    companion object {
        const val QR_STRING_CONTENT_KEY = "qr_s"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
