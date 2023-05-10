package net.aliaslab.securecall.flexqrreader.playvision.camerax

import android.util.Log
import androidx.camera.core.ImageProxy
import java.lang.ref.WeakReference

interface ImageProxySynchronizable {

    val synchronizer: ImageProxySynchronizer
}

class ImageProxySynchronizer(private val numberOfClients: Int) {

    private var imageProxy: WeakReference<ImageProxy> = WeakReference(null)

    private var finishedClients: Int = 0

    fun assignProxy(imageP: ImageProxy) {
        if (imageProxy.get() != null) {
            if (imageProxy.get()!!.imageInfo.timestamp != imageP.imageInfo.timestamp) {
                Log.d(tag, "Edited proxy")
                imageProxy = WeakReference(imageP)
            }
        } else {
            Log.d(tag, "First assign proxy")
            imageProxy = WeakReference(imageP)
        }
    }

    fun clientDidFinish(proxy: ImageProxy) {

        if (imageProxy.get() == null) {
            Log.d(tag, "Null proxy, skipping...")
            return
        }

        if (imageProxy.get()!!.imageInfo.timestamp == proxy.imageInfo.timestamp) {
            finishedClients += 1
            Log.d(tag, "Increasing clients... $finishedClients")
            if (finishedClients == numberOfClients) {
                Log.d(tag, "Closing proxy.")
                proxy.close()
                imageProxy = WeakReference(null)
                finishedClients = 0
            }
        }

    }

    private companion object {
        private const val tag = "ImageProxySynchronizer"
    }
}