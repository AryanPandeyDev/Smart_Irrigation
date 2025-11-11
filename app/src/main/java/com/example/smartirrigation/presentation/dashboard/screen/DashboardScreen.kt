package com.example.smartirrigation.presentation.dashboard.screen

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.smartirrigation.presentation.dashboard.components.AppTopBar
import com.example.smartirrigation.presentation.dashboard.components.IconType
import com.example.smartirrigation.presentation.dashboard.components.InfoDisplayCard
import com.example.smartirrigation.presentation.dashboard.components.PumpControlDialog
import com.example.smartirrigation.presentation.dashboard.components.QuickActionCardsCombined
import com.example.smartirrigation.presentation.dashboard.components.WaterPumpToggleCard
import com.example.smartirrigation.presentation.dashboard.state.DashboardState
import com.example.smartirrigation.presentation.dashboard.state.DeviceState
import com.example.smartirrigation.presentation.dashboard.viewmodels.DashboardViewModel
import com.example.smartirrigation.presentation.ui.theme.AppTheme


@Composable
fun DashboardScreenWrapper(
    viewModel : DashboardViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
    navigateToSetupScreen : () -> Unit
) {
    val state = viewModel.state.collectAsState()

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
        onNotificationToggle = viewModel::onNotificationToggle,
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
}

@Composable
fun DashboardScreen(
    dashboardState: DashboardState,
    onNotificationToggle: () -> Unit,
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
                subtitle = "Smart Garden Control",
                isNotificationEnabled = dashboardState.isNotificationOn,
                onNotificationToggle = {
                    onNotificationToggle()
                }
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