package net.aliaslab.securecall.flexqrreader

import androidx.fragment.app.Fragment
import net.aliaslab.securecall.flexqrreader.playvision.camerax.CameraScannerXFragment
import net.aliaslab.securecall.flexqrreader.zxing.ZXScanFragment

public class DefaultAdaptiveScanningActivity: AdaptiveScanningActivity() {
    override fun googlePlayServicesFragment(): Fragment {
        return CameraScannerXFragment()
    }

    override fun zebraFragment(): Fragment {
        return ZXScanFragment()
    }

}