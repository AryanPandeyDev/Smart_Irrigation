package com.example.smartirrigation.presentation.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import com.example.smartirrigation.presentation.dashboard.foreground_service.PumpStatusService
import com.example.smartirrigation.presentation.dashboard.viewmodels.DashboardViewModel
import com.example.smartirrigation.presentation.permission.viewmodel.PermissionViewModel
import com.example.smartirrigation.presentation.utils.hasNotificationPermission


class PermissionProvider {

    companion object {
        val permissionsToRequest = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
        )
    }

    @Composable
    operator fun invoke(
        context: Activity,
        viewModel: DashboardViewModel,
        permissionViewModel: PermissionViewModel
    ): ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>> {

        val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { perms ->
                perms.forEach { (permission, isGranted) ->
                    permissionViewModel.onPermissionResult(
                        permission = permission,
                        isGranted = isGranted
                    )
                }

                viewModel.onPermissionAsked()

                if (hasNotificationPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
                    Intent(context, PumpStatusService::class.java).apply {
                        context.startService(this)
                    }
                }
            }
        )

        return multiplePermissionResultLauncher
    }
}
