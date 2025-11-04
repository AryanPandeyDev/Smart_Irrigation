package com.example.smartirrigation.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PumpStatus (
    val isIrrigating: Boolean
)

@Serializable
data class PumpStatusResponse (
    val status : String,
    @SerialName("relayStatus")
    val isIrrigating : Boolean
)