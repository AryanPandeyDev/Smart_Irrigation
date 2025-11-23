package com.example.smartirrigation.presentation.setup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartirrigation.data.local.preferences.DatastoreManager
import com.example.smartirrigation.domain.repositories.IrrigationRepository
import com.example.smartirrigation.domain.repositories.PreferencesRepository
import com.example.smartirrigation.presentation.setup.state.SetupState
import dagger.hilt.android.lifecycle.HiltViewModel
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
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            repository.getStatus()
                .collect { info ->
                    if (info == null) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isSuccess = false,
                            error = "Disconnected"
                        )
                    } else {
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