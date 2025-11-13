package com.example.smartirrigation.presentation.dashboard.screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartirrigation.presentation.dashboard.components.AppTopBar
import com.example.smartirrigation.presentation.dashboard.components.IconType
import com.example.smartirrigation.presentation.dashboard.components.InfoDisplayCard
import com.example.smartirrigation.presentation.dashboard.components.PumpControlDialog
import com.example.smartirrigation.presentation.dashboard.components.QuickActionCardsCombined
import com.example.smartirrigation.presentation.dashboard.components.WaterPumpToggleCard
import com.example.smartirrigation.presentation.dashboard.foreground_service.PumpStatusService
import com.example.smartirrigation.presentation.dashboard.state.DashboardState
import com.example.smartirrigation.presentation.dashboard.state.DeviceState
import com.example.smartirrigation.presentation.dashboard.viewmodels.DashboardViewModel
import com.example.smartirrigation.presentation.permission.LocationPermissionTextProvider
import com.example.smartirrigation.presentation.permission.NotificationPermissionTextProvider
import com.example.smartirrigation.presentation.permission.PermissionDialog
import com.example.smartirrigation.presentation.permission.viewmodel.PermissionViewModel
import com.example.smartirrigation.presentation.ui.theme.AppTheme
import com.example.smartirrigation.presentation.utils.hasNotificationPermission
import com.example.smartirrigation.presentation.utils.openAppSettings
import kotlinx.coroutines.flow.first


@Composable
fun DashboardScreenWrapper(
    viewModel : DashboardViewModel = hiltViewModel(),
    permissionViewModel: PermissionViewModel = viewModel(),
    context: Context = LocalContext.current,
    navigateToSetupScreen : () -> Unit
) {
    val state = viewModel.state.collectAsState()

    val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
    )

    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            permissionsToRequest.forEach { permission ->
                permissionViewModel.onPermissionResult(
                    permission = permission,
                    isGranted = perms[permission] == true
                )
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!viewModel.askedPermission.first()) {
            multiplePermissionResultLauncher.launch(permissionsToRequest)
            viewModel.onPermissionAsked()
            if (hasNotificationPermission(context)) {
                ContextCompat.startForegroundService(context, Intent(context, PumpStatusService::class.java))
            }
        }
    }

    // Restore original behavior: navigate immediately when not connected
    LaunchedEffect(state.value.isConnected) {
        if (!state.value.isConnected) {
            navigateToSetupScreen()
        }
    }



    DashboardScreen(
        dashboardState = state.value,
        onWaterPumpToggle = {
            viewModel.onWaterPumpToggle(
                callBackToast = {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            )
        },
        onSetIrrigationMode = {
            viewModel.onSetIrrigationMode(
                callBackToast = {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            )
        },
        onSetThreshold = {
            viewModel.onSetThreshold (
                threshold = it,
                callBackToast = {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            )
        },
        onShowDialogBox = { showDialog ->
            viewModel.onShowControlDialog(showDialog)
        },
        // use the ViewModel's formatter so UI doesn't duplicate logic
        formatValue = viewModel::formatAsPercent
    )

    permissionViewModel.visiblePermissionDialogQueue
        .reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                        LocationPermissionTextProvider()
                    }
                    Manifest.permission.POST_NOTIFICATIONS -> {
                        NotificationPermissionTextProvider()
                    }
                    else -> return@forEach
                },
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                    context as Activity,
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
fun DashboardScreen(
    dashboardState: DashboardState,
    onWaterPumpToggle: () -> Unit,
    onSetIrrigationMode : () -> Unit,
    onSetThreshold : (Int) -> Unit = {},
    onShowDialogBox : (showDialog : Boolean) -> Unit,
    formatValue: (String?) -> String
) {

    Surface(
        Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AppTopBar(
                title = "Smart Irrigation",
                subtitle = "Smart Garden Control"
            )

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InfoDisplayCard(
                        modifier = Modifier
                            .weight(1f),
                        title = "Soil Moisture",
                        // use provided formatter
                        value = formatValue(dashboardState.deviceState.soilMoisture),
                        info = "Current Level",
                        iconType = IconType.MOISTURE
                    )

                    InfoDisplayCard(
                        modifier = Modifier
                            .weight(1f),
                        title = "Threshold",
                        // use provided formatter
                        value = formatValue(dashboardState.deviceState.currentThreshold),
                        info = "Current Threshold",
                        iconType = IconType.THRESHOLD
                    )
                }
                WaterPumpToggleCard(
                    isPumpOn = dashboardState.deviceState.isIrrigating
                )
                QuickActionCardsCombined(
                    manualControlToggle = {
                        onShowDialogBox(true)
                    },
                    historyLogsToggle = {

                    }
                )
            }
        }
        if (dashboardState.showControlDialog) {
            PumpControlDialog(
                onDismiss = {
                    onShowDialogBox(false)
                },
                onPumpToggle = {
                    onWaterPumpToggle()
                },
                onThresholdSet = {
                    onSetThreshold(it)
                },
                isManualMode = dashboardState.deviceState.isManualMode,
                isPumpOn = (dashboardState.deviceState.isIrrigating == true),
                onSwitchModes = {
                    onSetIrrigationMode()
                },
                // Bind loading flags from state
                isPumpToggleLoading = dashboardState.isPumpToggleLoading,
                isModeSwitchLoading = dashboardState.isModeSwitchLoading,
                isThresholdSetLoading = dashboardState.isThresholdSetLoading,
            )
        }
    }
}

//@Preview(
//    name = "Light Mode",
//    showBackground = true,
//    uiMode = Configuration.UI_MODE_NIGHT_NO
//)
//@Composable
//fun DashboardScreenPreview() {
//    AppTheme {
//        DashboardScreen(
//            dashboardState = DashboardState(
//                isNotificationOn = true,
//                deviceState = DeviceState(
//                    soilMoisture = "650",
//                    currentThreshold = "900",
//                    isIrrigating = false
//                )
//            ),
//            onNotificationToggle = {
//
//            },
//            onWaterPumpToggle = {},
//            onSetIrrigationMode = {},
//            formatValue = { valueStr ->
//                if (valueStr.isNullOrBlank()) return@DashboardScreen "N/A"
//                val num = valueStr.toIntOrNull() ?: return@DashboardScreen "N/A"
//                val percent = ((num.coerceIn(0, 1024).toFloat() / 1024f) * 100f).toInt()
//                "${percent}% (${num})"
//            }
//        ) {
//
//        }
//    }
//}
//
//@Preview(
//    name = "Dark Mode",
//    showBackground = true,
//    uiMode = Configuration.UI_MODE_NIGHT_YES
//)
//@Composable
//fun DashboardScreenPreviewDark() {
//    AppTheme(
//        darkTheme = true
//    ) {
//        DashboardScreen(
//            dashboardState = DashboardState(
//                isNotificationOn = true,
//                deviceState = DeviceState(
//                    soilMoisture = "650",
//                    currentThreshold = "900",
//                    isIrrigating = false
//                )
//            ),
//            onNotificationToggle = {
//
//            },
//            onWaterPumpToggle = {},
//            onSetIrrigationMode = {},
//            formatValue = { valueStr ->
//                if (valueStr.isNullOrBlank()) return@DashboardScreen "N/A"
//                val num = valueStr.toIntOrNull() ?: return@DashboardScreen "N/A"
//                val percent = ((num.coerceIn(0, 1024).toFloat() / 1024f) * 100f).toInt()
//                "${percent}% (${num})"
//            }
//        ) {
//
//        }
//    }
//}