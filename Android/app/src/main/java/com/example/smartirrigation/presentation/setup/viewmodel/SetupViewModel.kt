package com.example.smartirrigation.presentation.setup.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartirrigation.data.local.preferences.DatastoreManager
import com.example.smartirrigation.domain.repositories.IrrigationRepository
import com.example.smartirrigation.domain.repositories.PreferencesRepository
import com.example.smartirrigation.presentation.setup.state.SetupState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    val repository: IrrigationRepository
) : ViewModel() {
    var _state : MutableStateFlow<SetupState> = MutableStateFlow(SetupState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true)
        Log.d("SetupViewModel", "refresh called")
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("SetupViewModel", "refresh called in IO")
            repository.getStatus()
                .collect { info ->
                    if (info == null) {
                        Log.d("SetupViewModel", "Disconnected")
                        withContext (Dispatchers.Main) {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                isSuccess = false,
                                error = "Disconnected"
                            )
                        }
                    } else {
                        Log.d("SetupViewModel", "Connected")
                        withContext (Dispatchers.Main) {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                isSuccess = true,
                                error = null
                            )
                        }
                    }
                }
        }

    }
}