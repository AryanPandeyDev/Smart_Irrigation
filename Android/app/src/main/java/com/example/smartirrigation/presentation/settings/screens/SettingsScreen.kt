package com.example.smartirrigation.presentation.settings.screens

import SettingsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.smartirrigation.presentation.dashboard.components.AppTopBar
import com.example.smartirrigation.presentation.settings.components.EditValueDialog
import com.example.smartirrigation.presentation.settings.components.SettingsItem
import com.example.smartirrigation.presentation.settings.components.SettingsSection
import com.example.smartirrigation.presentation.settings.components.SettingsToggle
import com.example.smartirrigation.presentation.settings.viewmodel.SettingsViewModel

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartirrigation.presentation.dashboard.foreground_service.PumpStatusService
import com.example.smartirrigation.presentation.dashboard.viewmodels.DashboardViewModel
import com.example.smartirrigation.presentation.permission.NotificationPermissionTextProvider
import com.example.smartirrigation.presentation.permission.PermissionDialog
import com.example.smartirrigation.presentation.permission.PermissionProvider
import com.example.smartirrigation.presentation.permission.viewmodel.PermissionViewModel
import com.example.smartirrigation.presentation.utils.hasNotificationPermission
import com.example.smartirrigation.presentation.utils.openAppSettings

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    permissionViewModel: PermissionViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val permissionLauncherProvider = PermissionProvider()
    val multiplePermissionResultLauncher = permissionLauncherProvider(
        context = context as Activity,
        viewModel = dashboardViewModel,
        permissionViewModel = permissionViewModel
    )

    LaunchedEffect(state.isNotificationsEnabled) {
        if (state.isNotificationsEnabled) {
            if (hasNotificationPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
                Intent(context, PumpStatusService::class.java).also {
                    context.startService(it)
                }
            }
        } else {
            Intent(context, PumpStatusService::class.java).also {
                context.stopService(it)
            }
        }
    }

    SettingsContent(
        state = state,
        onDarkModeToggle = viewModel::onDarkModeToggle,
        onNotificationsToggle = { enabled ->
            if (enabled) {
                if (hasNotificationPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
                    viewModel.onNotificationsToggle(true)
                } else {
                    multiplePermissionResultLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }
            } else {
                viewModel.onNotificationsToggle(false)
            }
        },
        onEditPlantClick = viewModel::onEditPlantClick,
        onEditLocationClick = viewModel::onEditLocationClick,
        onDismissDialog = viewModel::onDismissDialog,
        onSavePlantName = viewModel::onSavePlantName,
        onSaveLocation = viewModel::onSaveLocation
    )

    permissionViewModel.visiblePermissionDialogQueue
        .reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.POST_NOTIFICATIONS -> {
                        NotificationPermissionTextProvider()
                    }
                    else -> return@forEach
                },
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                    context,
                    permission
                ),
                onDismiss = permissionViewModel::dismissDialog,
                onOkClick = {
                    permissionViewModel.dismissDialog()
                    multiplePermissionResultLauncher.launch(
                        arrayOf(permission)
                    )
                },
                onGoToAppSettingsClick = {
                    context.openAppSettings()
                }
            )
        }
}

@Composable
fun SettingsContent(
    state: SettingsState,
    onDarkModeToggle: (Boolean) -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onEditPlantClick: () -> Unit,
    onEditLocationClick: () -> Unit,
    onDismissDialog: () -> Unit,
    onSavePlantName: (String) -> Unit,
    onSaveLocation: (String) -> Unit
) {
    if (state.showPlantDialog) {
        EditValueDialog(
            title = "Edit Plant Name",
            initialValue = state.plantName,
            onDismiss = onDismissDialog,
            onSave = onSavePlantName
        )
    }

    if (state.showLocationDialog) {
        EditValueDialog(
            title = "Edit Location",
            initialValue = state.userLocation,
            onDismiss = onDismissDialog,
            onSave = onSaveLocation
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                subtitle = "Manage your preferences"
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            SettingsSection(title = "Preferences") {
                SettingsToggle(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Toggle application dark theme",
                    checked = state.isDarkModeEnabled,
                    onCheckedChange = onDarkModeToggle
                )
                SettingsToggle(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Receive alerts about your plants",
                    checked = state.isNotificationsEnabled,
                    onCheckedChange = onNotificationsToggle
                )
            }

            SettingsSection(title = "Configuration") {
                SettingsItem(
                    icon = Icons.Default.LocalFlorist,
                    title = "Edit Plant",
                    subtitle = state.plantName.ifEmpty { "Tap to set plant name" },
                    onClick = onEditPlantClick
                )
                SettingsItem(
                    icon = Icons.Default.LocationOn,
                    title = "Edit Location",
                    subtitle = state.userLocation.ifEmpty { "Tap to set location" },
                    onClick = onEditLocationClick
                )
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsContent(
            state = SettingsState(
                isDarkModeEnabled = false,
                isNotificationsEnabled = true,
                plantName = "My Awesome Plant",
                userLocation = "New York, USA"
            ),
            onDarkModeToggle = {},
            onNotificationsToggle = {},
            onEditPlantClick = {},
            onEditLocationClick = {},
            onDismissDialog = {},
            onSavePlantName = {},
            onSaveLocation = {}
        )
    }
}