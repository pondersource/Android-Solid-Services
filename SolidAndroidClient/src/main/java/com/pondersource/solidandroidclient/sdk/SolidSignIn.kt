package com.pondersource.solidandroidclient.sdk

import android.content.Context

class SolidSignIn {

    companion object {

        fun getClient(context: Context): SolidSignInClient {
            return SolidSignInClient.getInstance(context, context.applicationInfo,
                hasInstalledAndroidSolidServices = {
                    hasInstalledAndroidSolidServices(context)
                })
        }
    }
}