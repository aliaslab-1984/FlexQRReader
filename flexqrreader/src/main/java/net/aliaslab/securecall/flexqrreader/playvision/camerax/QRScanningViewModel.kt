package net.aliaslab.securecall.flexqrreader.playvision.camerax

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.common.Barcode

public interface BarcodeListener {

    var barcodes: MutableLiveData<List<Barcode>>

}

public class QRScanningViewModel: ViewModel(), BarcodeListener {

    override var barcodes = MutableLiveData<List<Barcode>>()

}