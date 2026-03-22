package com.example.smartirrigation.data.network.dto.chatbot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Single token in the SSE streaming response.
 */
@Serializable
data class StreamToken(
    val token: String,
    val action: ActionPayload? = null
)

/**
 * Action payload that the app should execute.
 * Returned by the AI when user requests a change.
 */
@Serializable
data class ActionPayload(
    val type: String,
    val value: Int? = null,
    val mode: Boolean? = null,
    @SerialName("pump_status")
    val pumpStatus: Boolean? = null,
    val reason: String? = null
) {
    companion object {
        const val TYPE_SET_THRESHOLD = "set_threshold"
        const val TYPE_SET_MODE = "set_mode"
        const val TYPE_SET_PUMP = "set_pump"
    }
}

/**
 * Full response for sync (non-streaming) endpoint.
 */
@Serializable
data class ChatResponse(
    val response: String,
    val action: ActionPayload? = null
)
