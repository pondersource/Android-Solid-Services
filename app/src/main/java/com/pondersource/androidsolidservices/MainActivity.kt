package com.pondersource.androidsolidservices

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pondersource.androidsolidservices.databinding.ActivityMainBinding


class MainActivity: AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var authViewModel: AuthViewModel
    private lateinit var binding: ActivityMainBinding

    private lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel = AuthViewModel.getInstance(this)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        handleAccountManagement()

        binding.logoutBtn.setOnClickListener {
            authViewModel.logout()
            val deleteRes = AccountManager.get(this).removeAccountExplicitly(account)
            if (deleteRes) {
                showMessage("Solid account removed from your phone successfully.")
            } else {
                showMessage("There is a problem in removing your Solid account from your phone.")
            }
            startActivity(Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }
    }

    fun handleAccountManagement() {
        val accountManager = AccountManager.get(this)
        account = Account(
            resources.getString(R.string.solid_account_acc_type),
            resources.getString(R.string.solid_account_acc_type)
        )

        var hasAddedBefore = false
        accountManager.accounts.forEach { acc ->
            if (acc.type == account.type && acc.name == account.name) {
                hasAddedBefore = true
            }
        }

        if (!hasAddedBefore) {
            val success = accountManager.addAccountExplicitly(account, "password", null)
            if (success) {
                showMessage("Account added to your phone successfully.")
            } else {
                showMessage("There is a problem in adding your Solid account to your phone.")
            }
        }

    }
}