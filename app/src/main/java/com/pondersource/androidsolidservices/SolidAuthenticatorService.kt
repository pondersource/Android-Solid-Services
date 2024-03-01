package com.pondersource.androidsolidservices

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SolidAuthenticatorService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        val authenticator = SolidAuthenticator(this)
        return authenticator.iBinder
    }
}