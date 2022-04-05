package net.aliaslab.securecall.flexqrreader.playvision.camerax

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(private val barcodes: MutableLiveData<List<Barcode>>) : ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
            val scanner = BarcodeScanning.getClient(options)

            scanner.process(image)
                .addOnSuccessListener {
                    if (it.isNotEmpty()) {
                        Log.d("BarcodeAnalyzer", "Found items! " + it.first().rawValue)
                        barcodes.postValue(it)
                    }

                    imageProxy.close()
                }
                .addOnFailureListener {
                    Log.e("BarcodeAnalyzer", "Detection failed", it)
                    imageProxy.close()
                }
        }
    }
}