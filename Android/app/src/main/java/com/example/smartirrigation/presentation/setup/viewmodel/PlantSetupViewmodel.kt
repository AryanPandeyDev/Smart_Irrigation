package com.example.smartirrigation.presentation.setup.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartirrigation.data.network.dto.Threshold
import com.example.smartirrigation.domain.repositories.IrrigationRepository
import com.example.smartirrigation.domain.repositories.PreferencesRepository
import com.example.smartirrigation.presentation.setup.state.PlantSetupUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class PlantSetupViewModel @Inject constructor(
    val irrigationRepoImpl : IrrigationRepository,
    val preferencesRepoImpl : PreferencesRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(PlantSetupUiState())
    var uiState = _uiState.asStateFlow()


    fun updatePlantType(value: String) {
        _uiState.value = _uiState.value.copy(plantType = value)
        setErrorValues()
    }

    fun updateThreshold(value: String) {
        _uiState.value = _uiState.value.copy(threshold = value)
        setErrorValues()
    }

    fun onSave(threshold: Int, launchToast : (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val plantSaved = preferencesRepoImpl.savePlantInfo(_uiState.value.plantType)
            val thresholdReal = ceil((threshold / 100.0) * 1024).toInt()
            if (irrigationRepoImpl.setThreshold(thresholdReal) && plantSaved) {
                withContext(Dispatchers.Main) {
                    launchToast("Threshold set successfully")
                }
            }else {
                withContext(Dispatchers.Main) {
                    launchToast("Failed to set threshold")
                }
            }
        }
    }

    fun setErrorValues() {
        _uiState.value = _uiState.value.copy(
            errors = _uiState.value.errors.copy(
                if (_uiState.value.plantType.isBlank()) "Plant type cannot be empty" else null,
                 thresholdInvalid = if (_uiState.value.threshold.toIntOrNull() == null || _uiState.value.threshold.toInt() < 0 || _uiState.value.threshold.toInt() > 1023) "Threshold must be a number between 0 and 1023" else null,
                thresholdEmpty = if (_uiState.value.threshold.isBlank()) "Threshold cannot be empty" else null
            )
        )
    }

    fun setIsEnabled() : Boolean {
        if (_uiState.value.plantType.isNotBlank() &&
            _uiState.value.threshold.isNotBlank() &&
            _uiState.value.threshold.toInt() >= 0 &&
            _uiState.value.threshold.toInt() <= 1023) {
            return true
        } else {
            Log.d("PlantSetupViewModel", "setIsEnabled")
            return false
        }
    }
}
