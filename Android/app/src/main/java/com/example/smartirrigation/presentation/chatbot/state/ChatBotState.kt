package com.example.smartirrigation.presentation.chatbot.state

import com.example.smartirrigation.data.network.dto.chatbot.ActionPayload

/**
 * Represents a single chat message in the conversation.
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val isStreaming: Boolean = false,
    val action: ActionPayload? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * UI state for the chatbot screen.
 */
data class ChatBotState(
    val messages: List<ChatMessage> = emptyList(),
    val currentInput: String = "",
    val isLoading: Boolean = false,
    val isConnected: Boolean = true,
    val error: String? = null,
    val pendingAction: ActionPayload? = null
)
