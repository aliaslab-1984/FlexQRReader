package net.aliaslab.securecall.flexqrreader

import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import net.aliaslab.securecall.flexqrreader.playvision.camerax.CameraScannerXFragment
import net.aliaslab.securecall.flexqrreader.zxing.ZXScanFragment

/**
 * Implementation of a default behavior for the QR-Scanning Activity.
 * It uses a super basic layout, with no extra buttons or labels.
 */
public class DefaultAdaptiveScanningActivity: AdaptiveScanningActivity() {

    override fun googlePlayServicesFragment(): Fragment {
        return CameraScannerXFragment()
    }

    override fun zebraFragment(): Fragment {
        return ZXScanFragment()
    }

    override fun scanQr() {
        setContentView(R.layout.activity_adaptive_scanning)
        replaceCurrentLayout(getCorrectFragment())
        isScanningQR = true
    }

    private fun replaceCurrentLayout(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.adaptive_main_frame_layout, fragment)
            setPrimaryNavigationFragment(fragment)
        }
    }

}