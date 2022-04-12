package net.aliaslab.securecall.flexqrreader.playvision.camerax

import android.os.Bundle
import androidx.fragment.app.commit
import net.aliaslab.securecall.flexqrreader.R
import net.aliaslab.securecall.flexqrreader.playvision.QRScannerActivity

class QRScanActivityX : QRScannerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscan_x)

        supportActionBar?.setTitle(R.string.qr_scan_title)
        supportActionBar?.setSubtitle(R.string.qr_scan_subtitle)

        val fragment = CameraScannerXFragment()
        supportFragmentManager.commit {
            replace(R.id.main_frame_layout, fragment)
            setPrimaryNavigationFragment(fragment)
        }
    }
}