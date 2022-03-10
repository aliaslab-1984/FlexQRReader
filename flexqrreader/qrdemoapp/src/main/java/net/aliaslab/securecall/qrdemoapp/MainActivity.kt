package net.aliaslab.securecall.qrdemoapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import net.aliaslab.securecall.flexqrreader.DefaultQRActivity

class MainActivity : AppCompatActivity() {

    private val qrActivityLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, DefaultQRActivity::class.java)
        qrActivityLauncher.launch(intent)
    }
}