package com.pondersource.androidsolidservices.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.pondersource.androidsolidservices.R
import com.pondersource.androidsolidservices.ui.navigation.Login

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Setting(
    navController: NavController,
    viewModel : SettingViewModel
) {

    LaunchedEffect(viewModel.logoutResult.value) {
        if (viewModel.logoutResult.value == true) {
            navController.navigate(Login) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.setting),
                    )
                }
            )
        }
    ) { paddingValues ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Button(
                onClick = {
                    viewModel.logout()
                }
            ) {
                Text(
                    text = stringResource(R.string.logout),
                )
            }
        }
    }
}