package net.aliaslab.securecall.flexqrreader.playvision.camerax.analyzer

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.text.Text
import net.aliaslab.securecall.flexqrreader.playvision.camerax.ImageProxySynchronizable
import net.aliaslab.securecall.flexqrreader.playvision.camerax.ImageProxySynchronizer

class FaceAndTextAnalyzer(private val face: MutableLiveData<Face>,
                          private val checksOpenedEyes: Boolean,
                          private val texts: MutableLiveData<List<Text.TextBlock>>,
                          override val synchronizer: ImageProxySynchronizer = ImageProxySynchronizer(2)
): ImageAnalysis.Analyzer, ImageProxySynchronizable {

    private var textAnalyzer: TextAnalyzer = TextAnalyzer(texts, synchronizer)
    private var faceAnalyzer: FaceAnalyzer = FaceAnalyzer(face, checksOpenedEyes, synchronizer)

    override fun analyze(image: ImageProxy) {
        textAnalyzer.analyze(image)
        faceAnalyzer.analyze(image)
    }
}