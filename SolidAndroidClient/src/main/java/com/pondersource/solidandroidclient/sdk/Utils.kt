package com.pondersource.solidandroidclient.sdk

import android.content.Context
import android.content.pm.PackageManager
import com.pondersource.solidandroidclient.ANDROID_SOLID_SERVICES_PACKAGE_NAME

fun hasInstalledAndroidSolidServices(context: Context): Boolean {
    try {
        context.packageManager.getPackageInfo(ANDROID_SOLID_SERVICES_PACKAGE_NAME, 0)
        return true
    } catch (_: PackageManager.NameNotFoundException) {}
    return false
}