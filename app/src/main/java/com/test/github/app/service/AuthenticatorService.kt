package com.test.github.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.test.github.data.account.AccountAuthenticator

class AuthenticatorService : Service() {

    private var authenticator: AccountAuthenticator? = null

    override fun onCreate() {
        super.onCreate()
        authenticator = AccountAuthenticator(applicationContext)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return authenticator?.iBinder
    }
}