package net.aliaslab.securecall.flexqrreader.utils

import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat

object Utils {

    fun getPackageInfo(activity: Activity?): PackageInfo? {

        if (activity == null || activity.isFinishing) {
            return null
        }
        try {
            return activity.packageManager.getPackageInfo(activity.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("Utils", e.message ?: "Empty message")
        }
        return null
    }

    fun getPackageVersionName(activity: Activity?): String {
        val pInfo = getPackageInfo(activity)
        return pInfo?.versionName ?: ""
    }

    fun getPackageVersionName(pInfo: PackageInfo?): String {
        return pInfo?.versionName ?: ""
    }

    fun getPackageBuild(pInfo: PackageInfo?): String {
        return if (pInfo != null) PackageInfoCompat.getLongVersionCode(pInfo).toString() else ""
    }
}