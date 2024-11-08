package com.pondersource.androidsolidservices.ui.elements

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.pondersource.androidsolidservices.model.GrantedApp

@Composable
fun RevokePermissionDialog(
    app: GrantedApp,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
){
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onConfirm) {
                Text("Revoke")
            }
        },
        dismissButton = {
            Button(onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text("Revoke Permission")
        },
        text = {
            Text("Do you want to revoke Solid access from ${app.name}?")
        }
    )
}

@Composable
fun RequestPermissionDialog(
    appName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
){
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onConfirm) {
                Text("Allow")
            }
        },
        modifier = Modifier.size(100.dp),
        dismissButton = {
            Button(onDismiss) {
                Text("Deny")
            }
        },
        title = {
            Text("Permission Request")
        },
        text = {
            Text("$appName wants to access your Solid pod. Do you allow?")
        },
        properties = DialogProperties(false, false)
    )
}