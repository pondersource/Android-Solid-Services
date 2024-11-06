package com.pondersource.androidsolidservices

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ASSApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }
}