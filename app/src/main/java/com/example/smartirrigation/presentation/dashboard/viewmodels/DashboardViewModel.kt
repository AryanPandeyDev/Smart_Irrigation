package com.example.smartirrigation.presentation.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartirrigation.data.network.dto.Threshold
import com.example.smartirrigation.domain.repositories.IrrigationRepository
import com.example.smartirrigation.presentation.dashboard.state.DashboardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    val repository: IrrigationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()

    init {
        startCollectingData()
    }

    fun startCollectingData() {
        viewModelScope.launch {
            repository.getStatus()
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

    fun onWaterPumpToggle(callBackToast : (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.turnOnPump(!_state.value.deviceState.isIrrigating)) {
                // ensure callback runs on Main (UI) thread
                withContext(Dispatchers.Main) {
                    callBackToast(
                        if (_state.value.deviceState.isIrrigating) {
                            "Water Pump Turned ON"
                        } else {
                            "Water Pump Turned OFF"
                        }
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    callBackToast("Failed to toggle Water Pump")
                }
            }
        }
    }



    fun onShowControlDialog(showDialog : Boolean) {
        _state.value = _state.value.copy(
            showControlDialog = showDialog
        )
    }

    fun onSetIrrigationMode(callBackToast : (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO){
            if (repository.setControlMode(!_state.value.deviceState.isManualMode)) {
                withContext(Dispatchers.Main) {
                    callBackToast(
                        if (_state.value.deviceState.isManualMode) {
                            "Manual Mode Enabled"
                        } else {
                            "Automatic Mode Enabled"
                        }
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    callBackToast("Failed to change Irrigation Mode")
                }
            }
        }
    }

    fun onSetThreshold(callBackToast : (String) -> Unit,threshold: Int?) {
        if (threshold != null) {
            viewModelScope.launch(Dispatchers.IO) {
                if (repository.setThreshold(threshold)) {
                    withContext(Dispatchers.Main) {
                        callBackToast("Threshold set successfully")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callBackToast("Failed to set Threshold")
                    }
                }
            }
        } else {
            callBackToast("Invalid Threshold value")
        }
    }
    fun onNotificationToggle() {
        _state.value = _state.value.copy(
            isNotificationOn = !_state.value.isNotificationOn
        )
    }
}