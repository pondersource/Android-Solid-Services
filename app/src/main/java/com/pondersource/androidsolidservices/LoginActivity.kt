package com.pondersource.androidsolidservices

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
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
            authViewModel.submitAuthorizationResponse(resp, ex)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        authViewModel = AuthViewModel.getInstance(this)

        setContentView(binding.root)

        authViewModel.loginLoading.observe(this) {
            binding.loginSolidcommunityBtn.isEnabled = !it
            binding.loginInruptBtn.isEnabled = !it
            if (it) {
                binding.pbLogin.visibility = VISIBLE
            } else {
                binding.pbLogin.visibility = GONE
            }
        }
        authViewModel.loginBrowserIntentErrorMessage.observe(this) {
            if (!it.isNullOrEmpty()) {
                showMessage(it)
            }
        }
        authViewModel.loginBrowserIntent.observe(this) {
            if (it != null) {
                doAuthenticationInBrowser.launch(it)
            }
        }
        authViewModel.loginResult.observe(this) {
            if(it == true) {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
            }
        }

        binding.loginInruptBtn.setOnClickListener {
            authViewModel.loginWithInruptCom()
        }

        binding.loginSolidcommunityBtn.setOnClickListener {
            authViewModel.loginWithSolidcommunity()
        }
    }
}