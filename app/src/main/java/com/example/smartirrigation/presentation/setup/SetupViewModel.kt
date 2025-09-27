package com.example.smartirrigation.presentation.setup

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartirrigation.domain.repositories.IrrigationRepository
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    val repository: IrrigationRepository
) : ViewModel() {
    var _state : MutableStateFlow<SetupState> = MutableStateFlow(SetupState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            refresh()
        }
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch(Dispatchers.IO){
            val status = repository.getStatus()
            if (status != null) {
                _state.value = _state.value.copy(isSuccess = true, isLoading = false)
            } else {
                _state.value = _state.value.copy(isSuccess = false, isLoading = false, error = "Failed to connect to the device. Please ensure you are connected to the device's Wi-Fi.")
            }
        }
    }

}