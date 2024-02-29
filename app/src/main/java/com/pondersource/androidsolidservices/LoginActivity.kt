package com.pondersource.androidsolidservices

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.pondersource.androidsolidservices.databinding.ActivityLoginBinding
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

class LoginActivity: AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var authViewModel: AuthViewModel

    private val doAuthenticationInBrowser = registerForActivityResult(object : ActivityResultContract<Intent, Intent?>() {
        override fun createIntent(context: Context, input: Intent): Intent {
            return input
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Intent? {
            return intent
        }

    }) { intent: Intent? ->
        if (intent != null) {
            val resp: AuthorizationResponse? = AuthorizationResponse.fromIntent(intent)
            val ex: AuthorizationException? = AuthorizationException.fromIntent(intent)
            if (resp != null) {
                authViewModel.requestToken(resp, ex) { tokenResponse, authorizationException ->
                    if (tokenResponse != null) {
                        authViewModel.saveLoginInfo()
                        startActivity(Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        })
                    } else {

                    }
                }
            } else {

            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        authViewModel = AuthViewModel.getInstance(this)

        setContentView(binding.root)

        if (authViewModel.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }

        binding.loginInruptBtn.setOnClickListener {
            authViewModel.loginWithInruptCom { intent, errorMessage ->
                if (intent != null) {
                    doAuthenticationInBrowser.launch(intent)
                } else if (!errorMessage.isNullOrEmpty()) {
                    showMessage(errorMessage)
                }
            }
        }

        binding.loginSolidcommunityBtn.setOnClickListener {
            authViewModel.loginWithSolidcommunity { intent, errorMessage ->
                if (intent != null) {
                    doAuthenticationInBrowser.launch(intent)
                } else if (!errorMessage.isNullOrEmpty()) {
                    showMessage(errorMessage)
                }
            }
        }
    }
}