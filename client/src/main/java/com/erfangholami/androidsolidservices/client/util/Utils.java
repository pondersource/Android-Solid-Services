package com.erfangholami.androidsolidservices.client.util;

import android.content.Context;
import android.content.pm.PackageManager;

import com.erfangholami.androidsolidservices.client.internal.ConstantsKt;

public class Utils {
    public static boolean hasAndroidSolidServicesInstalled(Context context) {
        return hasApplicationInstalled(context, ConstantsKt.ANDROID_SOLID_SERVICES_PACKAGE_NAME);
    }

    private static boolean hasApplicationInstalled(Context context, String applicationUri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(applicationUri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
