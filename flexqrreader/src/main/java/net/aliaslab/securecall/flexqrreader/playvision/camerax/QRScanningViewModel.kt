package net.aliaslab.securecall.flexqrreader.playvision.camerax

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.common.Barcode

public interface BarcodeListener {

    var barcodes: MutableLiveData<List<Barcode>>
    var strings: MutableLiveData<List<String>>

}

public class QRScanningViewModel: ViewModel(), BarcodeListener {

    override var barcodes = MutableLiveData<List<Barcode>>()
    override var strings = MutableLiveData<List<String>>()

}