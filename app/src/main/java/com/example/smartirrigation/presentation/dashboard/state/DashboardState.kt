package com.example.smartirrigation.presentation.dashboard.state

import com.example.smartirrigation.data.network.dto.Threshold
import com.example.smartirrigation.presentation.setup.state.SetupState

data class DashboardState(
    val isNotificationOn : Boolean = false,
    val isConnected : Boolean = true,
    val showControlDialog : Boolean = false,
    // Loading flags for operations
    val isPumpToggleLoading: Boolean = false,
    val isModeSwitchLoading: Boolean = false,
    val isThresholdSetLoading: Boolean = false,
    val deviceState: DeviceState = DeviceState()
)

data class DeviceState(
    val soilMoisture: String = "",
    val currentThreshold: String = "",
    val isIrrigating: Boolean? = null,
    val isManualMode : Boolean = false,
)