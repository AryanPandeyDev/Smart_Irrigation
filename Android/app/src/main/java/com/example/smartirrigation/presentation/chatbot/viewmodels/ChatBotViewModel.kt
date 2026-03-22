package com.example.smartirrigation.presentation.chatbot.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartirrigation.data.network.dto.IrrigatorInfo
import com.example.smartirrigation.data.network.dto.chatbot.ActionPayload
import com.example.smartirrigation.data.network.dto.chatbot.ChatRequest
import com.example.smartirrigation.data.network.dto.chatbot.DeviceStateDto
import com.example.smartirrigation.domain.repositories.ChatBotRepository
import com.example.smartirrigation.domain.repositories.IrrigationRepository
import com.example.smartirrigation.domain.repositories.PreferencesRepository
import com.example.smartirrigation.presentation.chatbot.state.ChatBotState
import com.example.smartirrigation.presentation.chatbot.state.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatBotViewModel @Inject constructor(
    private val chatBotRepository: ChatBotRepository,
    private val irrigationRepository: IrrigationRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatBotState())
    val state = _state.asStateFlow()

    // Cache the latest device state
    private var latestDeviceState: IrrigatorInfo? = null
    private var userLocation: String? = null
    private var plantType: String? = null

    companion object {
        private const val TAG = "ChatBotViewModel"
    }

    init {
        // Collect device state in background
        viewModelScope.launch {
            irrigationRepository.getStatus().collect { info ->
                latestDeviceState = info
            }
        }

        // Load user preferences
        viewModelScope.launch {
            userLocation = preferencesRepository.getUserLocation()
            plantType = preferencesRepository.getPlantInfo()
        }
    }

    /**
     * Update the current input text.
     */
    fun onInputChange(input: String) {
        _state.value = _state.value.copy(currentInput = input)
    }

    /**
     * Send a message to the chatbot.
     */
    fun sendMessage() {
        val messageText = _state.value.currentInput.trim()
        if (messageText.isEmpty()) return

        // Add user message to chat
        val userMessage = ChatMessage(
            content = messageText,
            isFromUser = true
        )

        // Create placeholder for bot response
        val botMessageId = java.util.UUID.randomUUID().toString()
        val botMessage = ChatMessage(
            id = botMessageId,
            content = "",
            isFromUser = false,
            isStreaming = true
        )

        _state.value = _state.value.copy(
            messages = _state.value.messages + userMessage + botMessage,
            currentInput = "",
            isLoading = true,
            error = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Build request
                val request = buildChatRequest(messageText)
                
                var fullResponse = StringBuilder()
                var receivedAction: ActionPayload? = null

                // Collect streaming tokens
                chatBotRepository.sendMessage(request).collect { token ->
                    fullResponse.append(token.token)
                    
                    // Check for action
                    if (token.action != null) {
                        receivedAction = token.action
                    }

                    // Update the bot message with accumulated content
                    withContext(Dispatchers.Main) {
                        updateBotMessage(
                            messageId = botMessageId,
                            content = fullResponse.toString(),
                            isStreaming = true,
                            action = receivedAction
                        )
                    }
                }

                // Mark streaming complete
                withContext(Dispatchers.Main) {
                    updateBotMessage(
                        messageId = botMessageId,
                        content = fullResponse.toString(),
                        isStreaming = false,
                        action = receivedAction
                    )

                    _state.value = _state.value.copy(
                        isLoading = false,
                        pendingAction = receivedAction
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                withContext(Dispatchers.Main) {
                    updateBotMessage(
                        messageId = botMessageId,
                        content = "Error: ${e.message ?: "Failed to get response"}",
                        isStreaming = false
                    )
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    /**
     * Execute the pending action from the AI.
     */
    fun executeAction(action: ActionPayload, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = when (action.type) {
                ActionPayload.TYPE_SET_THRESHOLD -> {
                    action.value?.let { value ->
                        irrigationRepository.setThreshold(value)
                    } ?: false
                }
                ActionPayload.TYPE_SET_MODE -> {
                    action.mode?.let { mode ->
                        irrigationRepository.setControlMode(mode)
                    } ?: false
                }
                ActionPayload.TYPE_SET_PUMP -> {
                    action.pumpStatus?.let { status ->
                        irrigationRepository.turnOnPump(status)
                    } ?: false
                }
                else -> false
            }

            val message = if (success) {
                "Action executed successfully"
            } else {
                "Failed to execute action"
            }

            withContext(Dispatchers.Main) {
                _state.value = _state.value.copy(pendingAction = null)
                onResult(success, message)
            }
        }
    }

    /**
     * Dismiss the pending action without executing.
     */
    fun dismissAction() {
        _state.value = _state.value.copy(pendingAction = null)
    }

    /**
     * Clear all chat messages.
     */
    fun clearChat() {
        _state.value = _state.value.copy(
            messages = emptyList(),
            pendingAction = null,
            error = null
        )
    }

    private fun buildChatRequest(message: String): ChatRequest {
        val deviceState = latestDeviceState?.let {
            DeviceStateDto(
                soilMoisture = it.soilMoisture,
                threshold = it.threshold,
                relayStatus = it.relayStatus,
                mode = it.mode
            )
        } ?: DeviceStateDto(
            soilMoisture = 0,
            threshold = 512,
            relayStatus = false,
            mode = false
        )

        return ChatRequest(
            message = message,
            deviceState = deviceState,
            userLocation = userLocation,
            plantType = plantType
        )
    }

    private fun updateBotMessage(
        messageId: String,
        content: String,
        isStreaming: Boolean,
        action: ActionPayload? = null
    ) {
        _state.value = _state.value.copy(
            messages = _state.value.messages.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(
                        content = content,
                        isStreaming = isStreaming,
                        action = action
                    )
                } else {
                    msg
                }
            }
        )
    }
}
