package com.example.smartirrigation.presentation.history.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartirrigation.data.local.model.PumpLogEntry
import com.example.smartirrigation.domain.repositories.PumpLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryState(
    val logs: List<PumpLogEntry> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val pumpLogRepository: PumpLogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state = _state.asStateFlow()

    init {
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            pumpLogRepository.getLogEntries().collect { logs ->
                _state.value = _state.value.copy(
                    logs = logs,
                    isLoading = false
                )
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            pumpLogRepository.clearLogs()
        }
    }
}
