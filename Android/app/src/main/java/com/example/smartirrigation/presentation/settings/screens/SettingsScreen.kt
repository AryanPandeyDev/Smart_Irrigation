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

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    SettingsContent(
        state = state,
        onDarkModeToggle = viewModel::onDarkModeToggle,
        onNotificationsToggle = viewModel::onNotificationsToggle,
        onEditPlantClick = viewModel::onEditPlantClick,
        onEditLocationClick = viewModel::onEditLocationClick,
        onDismissDialog = viewModel::onDismissDialog,
        onSavePlantName = viewModel::onSavePlantName,
        onSaveLocation = viewModel::onSaveLocation
    )
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