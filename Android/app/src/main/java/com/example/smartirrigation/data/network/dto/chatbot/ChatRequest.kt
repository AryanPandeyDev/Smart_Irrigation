package com.example.smartirrigation.data.network.dto.chatbot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for the chat endpoint.
 */
@Serializable
data class ChatRequest(
    val message: String,
    @SerialName("device_state")
    val deviceState: DeviceStateDto,
    @SerialName("user_location")
    val userLocation: String? = null,
    @SerialName("plant_type")
    val plantType: String? = null
)
