package com.pondersource.solidandroidclient.sdk

import android.content.Context

/**
 * Entry point for the Android Solid Services client SDK.
 *
 * Third-party apps use this class to obtain the three SDK clients:
 * - [getSignInClient] — authenticate with Solid and manage authorization
 * - [getResourceClient] — read, create, update and delete Solid pod resources
 * - [getContactsDataModule] — access the Solid Contacts data module
 *
 * All three clients are singletons per application process and communicate with the
 * Android Solid Services app via AIDL bound services.
 *
 * **Prerequisites:** The Android Solid Services app must be installed and the user must
 * have logged in before calling resource or contacts operations.
 *
 * ### Typical setup (in `Application.onCreate` or a Hilt module):
 * ```kotlin
 * val signIn = Solid.getSignInClient(context)
 * signIn.requestLogin { granted, error -> ... }
 * ```
 */
class Solid {

    companion object {

        /**
         * Returns the [SolidSignInClient] singleton.
         *
         * Use this client to request login authorization, check sign-in status, and
         * disconnect from Solid.
         * @param context Any [Context]; the application context is used internally.
         */
        fun getSignInClient(context: Context): SolidSignInClient {
            return SolidSignInClient.getInstance(
                context,
                context.applicationInfo,
                hasInstalledAndroidSolidServices = {
                    hasInstalledAndroidSolidServices(context)
                }
            )
        }

        /**
         * Returns the [SolidResourceClient] singleton.
         *
         * Use this client to read, create, update and delete resources on the user's Solid pod.
         * Requires the user to be signed in via [getSignInClient].
         * @param context Any [Context]; the application context is used internally.
         */
        fun getResourceClient(context: Context): SolidResourceClient {
            return SolidResourceClient.getInstance(
                context,
                hasInstalledAndroidSolidServices = {
                    hasInstalledAndroidSolidServices(context)
                }
            )
        }

        /**
         * Returns the [SolidContactsDataModule] singleton.
         *
         * Use this module to manage address books, contacts and groups stored on the user's
         * Solid pod using the Solid Contacts specification.
         * Requires the user to be signed in via [getSignInClient].
         * @param context Any [Context]; the application context is used internally.
         */
        fun getContactsDataModule(context: Context): SolidContactsDataModule {
            return SolidContactsDataModule.getInstance(
                context,
            )
        }
    }
}