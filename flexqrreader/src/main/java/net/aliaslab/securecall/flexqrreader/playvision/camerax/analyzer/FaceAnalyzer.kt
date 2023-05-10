package net.aliaslab.securecall.flexqrreader.playvision.camerax.analyzer

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import net.aliaslab.securecall.flexqrreader.playvision.camerax.ImageProxySynchronizable
import net.aliaslab.securecall.flexqrreader.playvision.camerax.ImageProxySynchronizer
import net.aliaslab.securecall.flexqrreader.playvision.camerax.firstPredicateOrNull

class FaceAnalyzer(private val face: MutableLiveData<Face>,
                   private val checksOpenedEyes: Boolean,
                   override val synchronizer: ImageProxySynchronizer
) : ImageAnalysis.Analyzer, ImageProxySynchronizable {

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {

        Log.d(tag, "Starting face analysis")

        synchronizer.assignProxy(imageProxy)
        if (imageProxy.image == null) {
            synchronizer.clientDidFinish(imageProxy)
            return
        }

        val mediaImage = imageProxy.image ?: return

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        // Fast landmark detection and face classification
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            // I need mode to all in order to detect if the eyes are open or not.
            .setClassificationMode(if (checksOpenedEyes) FaceDetectorOptions.CLASSIFICATION_MODE_ALL else FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()

        val scanner = FaceDetection.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { scannedFaces ->

                val box = try {
                        scannedFaces.first().boundingBox
                    } catch (exception: java.lang.Exception) {
                        synchronizer.clientDidFinish(imageProxy)
                        null
                    } ?: return@addOnSuccessListener


                val x = box.left
                val y = box.top
                val width = box.width()
                val height = box.height()

                Log.d(tag, "Found a face! x: $x y: $y w: $width h: $height")

                val foundFace = scannedFaces.firstPredicateOrNull { face ->
                    if (face == null) {
                        false
                    } else {
                        if (checksOpenedEyes) {
                            val leftEyeProb = face.leftEyeOpenProbability ?: 0.0f
                            val rightEyeProb = face.rightEyeOpenProbability ?: 0.0f

                            Log.d(tag, "Eyes opened --> left: $leftEyeProb right: $rightEyeProb")

                            leftEyeProb > 0.6 || rightEyeProb > 0.6
                        } else {
                            true
                        }
                    }
                }

                foundFace.let {
                    face.postValue(it)
                }

                synchronizer.clientDidFinish(imageProxy)
            }
            .addOnFailureListener {
                Log.e(tag, "Detection failed", it)
                synchronizer.clientDidFinish(imageProxy)
            }

    }

    private companion object {
        private const val tag = "FaceAnalyzer"
    }
}