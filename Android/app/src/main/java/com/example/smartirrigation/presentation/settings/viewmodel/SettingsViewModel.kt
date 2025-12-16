package com.example.smartirrigation.presentation.settings.viewmodel

import SettingsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartirrigation.domain.repositories.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val plantName = preferencesRepository.getPlantInfo() ?: ""
            val userLocation = preferencesRepository.getUserLocation() ?: ""
            val isNotificationsEnabled = preferencesRepository.getNotificationPreference()
            val isDarkModeEnabled = preferencesRepository.getDarkModePreference()
            
            _state.update {
                it.copy(
                    plantName = plantName,
                    userLocation = userLocation,
                    isNotificationsEnabled = isNotificationsEnabled,
                    isDarkModeEnabled = isDarkModeEnabled
                )
            }
        }
    }

    fun onDarkModeToggle(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveDarkModePreference(enabled)
            AppState.isDarkMode.value = enabled
            _state.update { it.copy(isDarkModeEnabled = enabled) }
        }
    }

    fun onNotificationsToggle(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveNotificationPreference(enabled)
            _state.update { it.copy(isNotificationsEnabled = enabled) }
        }
    }

    fun onEditPlantClick() {
        _state.update { it.copy(showPlantDialog = true) }
    }

    fun onEditLocationClick() {
        _state.update { it.copy(showLocationDialog = true) }
    }

    fun onDismissDialog() {
        _state.update { it.copy(showPlantDialog = false, showLocationDialog = false) }
    }

    fun onSavePlantName(newName: String) {
        viewModelScope.launch {
            preferencesRepository.savePlantInfo(newName)
            _state.update {
                it.copy(
                    plantName = newName,
                    showPlantDialog = false
                )
            }
        }
    }

    fun onSaveLocation(newLocation: String) {
        viewModelScope.launch {
            preferencesRepository.saveUserLocation(newLocation)
            _state.update {
                it.copy(
                    userLocation = newLocation,
                    showLocationDialog = false
                )
            }
        }
    }
}
