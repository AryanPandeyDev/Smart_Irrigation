package com.example.smartirrigation.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class IrrigatorInfo(
    val threshold : Int,
    val soilMoisture : Int,
    val relayStatus : Boolean
)
