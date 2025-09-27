package com.example.smartirrigation.data.network.dto

data class IrrigatorInfo(
    val threshold : Int,
    val soilMoisture : Int,
    val relayStatus : Boolean
)
