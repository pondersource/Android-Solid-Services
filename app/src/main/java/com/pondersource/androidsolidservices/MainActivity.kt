package com.pondersource.androidsolidservices

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pondersource.androidsolidservices.databinding.ActivityMainBinding

class MainActivity: AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel = AuthViewModel.getInstance(this)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        authViewModel.testCRUD()

        binding.logoutBtn.setOnClickListener {
            authViewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }
    }
}