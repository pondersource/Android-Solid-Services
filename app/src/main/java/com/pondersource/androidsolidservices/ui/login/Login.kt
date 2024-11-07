package com.pondersource.androidsolidservices.ui.login

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Arrangement
import com.pondersource.androidsolidservices.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.pondersource.androidsolidservices.ui.navigation.MainPage
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

@Composable
fun Login(
    navController: NavHostController,
    viewModel: LoginViewModel,
) {

    val doAuthenticationInBrowser = rememberLauncherForActivityResult(object : ActivityResultContract<Intent, Intent?>() {
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
            viewModel.submitAuthorizationResponse(resp, ex)
        }
    }

    LaunchedEffect(viewModel.loginBrowserIntent.value) {
        if (viewModel.loginBrowserIntent.value != null) {
            doAuthenticationInBrowser.launch(viewModel.loginBrowserIntent.value!!)
        }
    }

    LaunchedEffect(viewModel.loginResult.value) {
        if(viewModel.loginResult.value == true) {
            navController.navigate(MainPage) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (viewModel.loginLoading.value == false) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        viewModel.loginWithInruptCom()
                    }
                ) {
                    Text(text = stringResource(R.string.login_with_inrupt))
                }

                Button(
                    onClick = {
                        viewModel.loginWithSolidcommunity()
                    }
                ) {
                    Text(text = stringResource(R.string.login_with_solidcommunity))
                }
            }
        } else if (viewModel.loginLoading.value == true) {
            CircularProgressIndicator()
        }
    }
}