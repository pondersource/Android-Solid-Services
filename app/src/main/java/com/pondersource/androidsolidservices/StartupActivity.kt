package com.pondersource.androidsolidservices

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pondersource.androidsolidservices.databinding.ActivityStartupBinding
import kotlinx.coroutines.launch

class StartupActivity: AppCompatActivity() {

    private lateinit var binding: ActivityStartupBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartupBinding.inflate(layoutInflater)

        authViewModel = AuthViewModel.getInstance(this)

        setContentView(binding.root)

        handleLogin()
    }

    private fun handleLogin() {
        lifecycleScope.launch {
            if (authViewModel.isLoggedIn()) {
                startActivity(Intent(this@StartupActivity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
            } else {
                startActivity(Intent(this@StartupActivity, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
            }
        }
    }
}