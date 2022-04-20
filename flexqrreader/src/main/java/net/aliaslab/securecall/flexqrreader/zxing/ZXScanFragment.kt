package net.aliaslab.securecall.flexqrreader.zxing

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.*
import net.aliaslab.securecall.flexqrreader.R
import net.aliaslab.securecall.flexqrreader.playvision.camerax.QRScanningViewModel
import java.util.*

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class ZXScanFragment : Fragment(), DecoratedBarcodeView.TorchListener, BarcodeCallback {

    private var capture: CaptureManager? = null
    private var barcodeScannerView: DecoratedBarcodeView? = null
    private var viewfinderView: ViewfinderView? = null

    private val viewModel: QRScanningViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_z_x_scan, container, false)

        barcodeScannerView = view?.findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView?.setTorchListener(this);

        viewfinderView = view?.findViewById(R.id.zxing_viewfinder_view);

        capture = CaptureManager(activity, barcodeScannerView)
        if (capture != null) {
            capture!!.initializeFromIntent(activity?.intent, savedInstanceState)
            capture!!.setShowMissingCameraPermissionDialog(false)

            barcodeScannerView?.barcodeView?.decodeContinuous(this)

            val formats: Collection<BarcodeFormat> =
                listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
            barcodeScannerView?.barcodeView?.decoderFactory = DefaultDecoderFactory(formats)
        }

        changeLaserVisibility(true);

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        capture?.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        capture?.onPause()
    }

    override fun onResume() {
        super.onResume()
        capture?.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture?.onSaveInstanceState(outState)
    }

    /**
     * Check if the device's camera has a Flashlight.
     * @return true if there is Flashlight, otherwise false.
     */
    private fun hasFlash(): Boolean {
        return requireContext().packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    override fun onTorchOn() {

    }

    override fun onTorchOff() {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun changeMaskColor(color: Color) {
        viewfinderView!!.setMaskColor(color.toArgb())
    }

    fun changeLaserVisibility(visible: Boolean) {
        viewfinderView!!.setLaserVisibility(visible)
    }

    override fun barcodeResult(result: BarcodeResult?) {
        if (result != null) {
            viewModel.strings.value = listOf(result.text)
        }
    }

}