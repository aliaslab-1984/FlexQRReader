package net.aliaslab.securecall.flexqrreader.zxing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit
import net.aliaslab.securecall.flexqrreader.R
import net.aliaslab.securecall.flexqrreader.playvision.QRScannerActivity
import net.aliaslab.securecall.flexqrreader.playvision.camerax.CameraScannerXFragment

class ZXScanActivity : QRScannerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zxscan)

        val fragment = ZXScanFragment()
        supportFragmentManager.commit {
            replace(R.id.main_camera_layout, fragment)
            setPrimaryNavigationFragment(fragment)
        }
    }
}