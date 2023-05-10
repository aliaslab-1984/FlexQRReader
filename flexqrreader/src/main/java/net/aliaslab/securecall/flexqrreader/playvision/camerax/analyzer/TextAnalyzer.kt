package net.aliaslab.securecall.flexqrreader.playvision.camerax.analyzer

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import net.aliaslab.securecall.flexqrreader.playvision.camerax.ImageProxySynchronizable
import net.aliaslab.securecall.flexqrreader.playvision.camerax.ImageProxySynchronizer

class TextAnalyzer(private val texts: MutableLiveData<List<Text.TextBlock>>,
                   override val synchronizer: ImageProxySynchronizer
): ImageAnalysis.Analyzer, ImageProxySynchronizable {

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {

        synchronizer.assignProxy(imageProxy)
        if (imageProxy.image == null) {
            synchronizer.clientDidFinish(imageProxy)
            return
        }

        val mediaImage = imageProxy.image ?: return

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        val scanner = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        scanner.process(image)
            .addOnSuccessListener { scannedTexts ->

                val textBlocks = scannedTexts.textBlocks

                if (textBlocks.isEmpty()) {
                    Log.d(tag, "Didn't found any text block.")
                    synchronizer.clientDidFinish(imageProxy)
                    return@addOnSuccessListener
                }

                Log.d(tag, "Found " + textBlocks.size + " text blocks.")

                textBlocks.forEach { textBlock ->
                    val box = textBlock.boundingBox
                    val x = box?.left
                    val y = box?.top
                    val width = box?.width()
                    val height = box?.height()

                    val text = textBlock.text

                    Log.d(tag, "Found a text block x: $x y: $y w: $width h: $height with text: $text")
                }

                texts.postValue(textBlocks)

                synchronizer.clientDidFinish(imageProxy)
            }
            .addOnFailureListener {
                Log.e(tag, "Detection failed", it)
                synchronizer.clientDidFinish(imageProxy)
            }

    }

    private companion object {
        private const val tag = "TextAnalyzer"
    }
}