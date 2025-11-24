package com.example.smartirrigation.presentation.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartirrigation.data.repositories.PreferencesRepoImpl
import com.example.smartirrigation.domain.repositories.IrrigationRepository
import com.example.smartirrigation.domain.repositories.PreferencesRepository
import com.example.smartirrigation.presentation.dashboard.state.DashboardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.roundToInt

@HiltViewModel
class DashboardViewModel @Inject constructor(
    val repository: IrrigationRepository,
    val preferencesRepoImpl: PreferencesRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val askedPermission = preferencesRepoImpl.askedPermissionFLow


    val state = _state.asStateFlow()

    init {
        startCollectingData()
    }

    fun startCollectingData() {
        viewModelScope.launch {
            repository.getStatus()
                .catch { _ ->
                    // Any error in SSE means we are disconnected
                    _state.value = _state.value.copy(isConnected = false)
                }
                .onCompletion {
                    // Stream ended (channel closed or request finished): mark disconnected
                    _state.value = _state.value.copy(isConnected = false)
                }
                .collect { irrigatorInfo ->
                    if (irrigatorInfo != null) {
                        _state.value = _state.value.copy(
                            isConnected = true,
                            deviceState = _state.value.deviceState.copy(
                                soilMoisture = irrigatorInfo.soilMoisture.toString(),
                                isIrrigating = irrigatorInfo.relayStatus,
                                currentThreshold = irrigatorInfo.threshold.toString(),
                                isManualMode = irrigatorInfo.mode
                            )
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isConnected = false
                        )
                    }
                }
        }
    }

    fun onPermissionAsked() {
        viewModelScope.launch {
            preferencesRepoImpl.changeAskedPermission(true)
        }
    }


    fun onWaterPumpToggle(callBackToast : (String) -> Unit) {
        // Set loading true (UI will disable/animate the button)
        _state.value = _state.value.copy(isPumpToggleLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.value.deviceState.isIrrigating?.let { current ->
                    val ok = repository.turnOnPump(!current)
                    withContext(Dispatchers.Main) {
                        if (ok) {
                            callBackToast(
                                if (current) {
                                    "Water Pump Turned ON"
                                } else {
                                    "Water Pump Turned OFF"
                                }
                            )
                        } else {
                            callBackToast("Failed to toggle Water Pump")
                        }
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        callBackToast("Unknown pump state")
                    }
                }
            } finally {
                // Always clear loading
                _state.value = _state.value.copy(isPumpToggleLoading = false)
            }
        }
    }

    fun onShowControlDialog(showDialog : Boolean) {
        _state.value = _state.value.copy(
            showControlDialog = showDialog
        )
    }

    fun onSetIrrigationMode(callBackToast : (String) -> Unit) {
        // Start switch loading
        _state.value = _state.value.copy(isModeSwitchLoading = true)
        viewModelScope.launch(Dispatchers.IO){
            try {
                val ok = repository.setControlMode(!_state.value.deviceState.isManualMode)
                withContext(Dispatchers.Main) {
                    if (ok) {
                        callBackToast(
                            if (_state.value.deviceState.isManualMode) {
                                "Manual Mode Enabled"
                            } else {
                                "Automatic Mode Enabled"
                            }
                        )
                    } else {
                        callBackToast("Failed to change Irrigation Mode")
                    }
                }
            } finally {
                _state.value = _state.value.copy(isModeSwitchLoading = false)
            }
        }
    }

    fun onSetThreshold(callBackToast : (String) -> Unit,threshold: Int?) {
        if (threshold != null) {
            // begin threshold loading
            _state.value = _state.value.copy(isThresholdSetLoading = true)
            val thresholdReal = ceil((threshold / 100.0) * 1024).toInt()
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val ok = repository.setThreshold(thresholdReal)
                    withContext(Dispatchers.Main) {
                        if (ok) {
                            callBackToast("Threshold set successfully")
                        } else {
                            callBackToast("Failed to set Threshold")
                        }
                    }
                } finally {
                    _state.value = _state.value.copy(isThresholdSetLoading = false)
                }
            }
        } else {
            callBackToast("Invalid Threshold value")
        }
    }


    // Value formatting for UI (kept as-is)
    fun formatAsPercent(valueStr: String?, max: Int = 1024): String {
        if (valueStr.isNullOrBlank()) return "N/A"
        val num = valueStr.toIntOrNull() ?: return "N/A"
        val percent = ((num.coerceIn(0, max).toFloat() / max) * 100).roundToInt()
        return "${percent}%"
    }
}