package com.pondersource.androidsolidservices.ui.main

import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import com.pondersource.androidsolidservices.R
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import com.pondersource.androidsolidservices.model.GrantedApp
import com.pondersource.androidsolidservices.ui.elements.RevokePermissionDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessGrants(
    navController: NavController,
    viewModel: AccessGrantViewModel,
) {

    val revokePermissionApp = remember { mutableStateOf<GrantedApp?>(null) }
    val showRevokePermissionDialog = remember { mutableStateOf(false) }

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
                    revokePermissionApp.value = app
                    showRevokePermissionDialog.value = true
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

        when {
            showRevokePermissionDialog.value -> {
                RevokePermissionDialog(
                    app = revokePermissionApp.value!!,
                    onDismiss = {
                        showRevokePermissionDialog.value = false
                        revokePermissionApp.value = null
                    },
                    onConfirm = {
                        viewModel.revokeAccess(revokePermissionApp.value!!)
                        showRevokePermissionDialog.value = false
                        revokePermissionApp.value = null
                    }
                )
            }
        }

    }
}

@Composable
private fun GrantedAppItem(
    app: GrantedApp,
    onRevokeClicked: () -> Unit
) {
    val context = LocalContext.current
    val icon = remember {
        try {
            context.packageManager.getApplicationIcon(app.packageName)
                .toBitmap(config = Bitmap.Config.ARGB_8888).asImageBitmap()
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(),
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                bitmap = icon ?: Bitmap.createBitmap(32.dp.value.toInt(), 32.dp.value.toInt(), Bitmap.Config.ARGB_8888).asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
            )

            Column {
                Text(text = app.name)
                Text(text = app.packageName)
                Button(onRevokeClicked) {
                    Text(text = "Revoke Permission")
                }
            }
        }
    }
}