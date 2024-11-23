package com.pondersource.solidandroidclient.sdk

import android.content.Context

class Solid {

    companion object {

        fun getSignInClient(context: Context): SolidSignInClient {
            return SolidSignInClient.getInstance(
                context,
                context.applicationInfo,
                hasInstalledAndroidSolidServices = {
                    hasInstalledAndroidSolidServices(context)
                }
            )
        }

        fun getResourceClient(context: Context): SolidResourceClient {
            return SolidResourceClient.getInstance(
                context,
                hasInstalledAndroidSolidServices = {
                    hasInstalledAndroidSolidServices(context)
                }
            )
        }

        fun getContactsDataModule(context: Context): SolidContactsDataModule {
            return SolidContactsDataModule.getInstance(
                context,
            )
        }
    }
}