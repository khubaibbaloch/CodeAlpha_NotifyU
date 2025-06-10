package com.notifyu.app.presentation.screens.main.components

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
fun RequestPermissions(
    permissions: List<String>,
    onAllGranted: () -> Unit = {},
    onDenied: (deniedPermissions: List<String>, permanentlyDenied: List<String>) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val permissionsToRequest = remember {
        mutableStateListOf<String>().apply {
            addAll(
                permissions.filter {
                    ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                }
            )
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val denied = result.filterValues { !it }.keys.toList()
        val permanentlyDenied = denied.filter {
            activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }

        if (denied.isEmpty()) {
            onAllGranted()
        } else {
            onDenied(denied, permanentlyDenied)
        }
    }

    LaunchedEffect(Unit) {
        if (permissionsToRequest.isNotEmpty() && activity != null) {
            launcher.launch(permissionsToRequest.toTypedArray())
        } else {
            onAllGranted()
        }
    }
}
