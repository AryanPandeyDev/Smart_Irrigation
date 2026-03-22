package com.example.smartirrigation.data.network.dto.chatbot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Device state to send to the AI chatbot.
 * Maps to the backend's DeviceState schema.
 */
@Serializable
data class DeviceStateDto(
    @SerialName("soil_moisture")
    val soilMoisture: Int,
    val threshold: Int,
    @SerialName("relay_status")
    val relayStatus: Boolean,
    val mode: Boolean
)
