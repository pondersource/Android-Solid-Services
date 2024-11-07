package com.pondersource.androidsolidservices.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import com.pondersource.androidsolidservices.R
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pondersource.androidsolidservices.model.GrantedApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessGrants(
    navController: NavController,
    viewModel: AccessGrantViewModel,
) {

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.granted_apps),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { paddingValues ->

        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(viewModel.grantedApps.value) { app ->
                GrantedAppItem(app) {
                    viewModel.revokeAccess(app)
                }
            }
            if (viewModel.grantedApps.value.isEmpty()) {
                item() {
                    Text(
                        text = "No app granted",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

    }
}

@Composable
private fun GrantedAppItem(
    app: GrantedApp,
    onRevokeClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(app.icon),
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .size(32.dp)
        )

        Column {
            Text(text = app.name)
            Text(text = app.packageName)
        }

        Button(onRevokeClicked) {
            Text(text = "Revoke")
        }
    }
}