package net.aliaslab.securecall.flexqrreader.playvision.camerax

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.text.Text

public interface BarcodeListener {

    var barcodes: MutableLiveData<List<Barcode>>
    var strings: MutableLiveData<List<String>>

}

public interface FaceListener {

    var foundFace: MutableLiveData<Face>

}

public interface TextListener {

    var textBlocks: MutableLiveData<List<Text.TextBlock>>

}

public class QRScanningViewModel() : ViewModel(), BarcodeListener, FaceListener, TextListener {

    override var barcodes = MutableLiveData<List<Barcode>>()
    override var strings = MutableLiveData<List<String>>()

    override var foundFace = MutableLiveData<Face>()
    override var textBlocks = MutableLiveData<List<Text.TextBlock>>()

}