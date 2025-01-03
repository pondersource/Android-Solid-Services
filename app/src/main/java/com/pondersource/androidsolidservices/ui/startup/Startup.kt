package com.pondersource.androidsolidservices.ui.startup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pondersource.androidsolidservices.R
import com.pondersource.androidsolidservices.ui.navigation.Login
import com.pondersource.androidsolidservices.ui.navigation.MainPage
import kotlinx.coroutines.launch

@Composable
fun Startup(
    navController: NavHostController,
    viewModel: StartupViewModel
) {

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            if (viewModel.isLoggedIn()) {
                navController.navigate(MainPage) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            } else {
                navController.navigate(Login) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.splash_loading))
        CircularProgressIndicator(
            modifier = Modifier
                .padding(24.dp)
                .size(32.dp)
        )
    }
}