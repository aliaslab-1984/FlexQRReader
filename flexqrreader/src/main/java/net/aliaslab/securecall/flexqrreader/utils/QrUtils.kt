package net.aliaslab.securecall.flexqrreader.utils

import android.app.Activity

object QrUtils {

    fun isHms(activity: Activity?): Boolean {
        return Utils.getPackageVersionName(activity).contains("-hms")
    }

}