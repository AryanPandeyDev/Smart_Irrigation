package com.example.smartirrigation.data.local.model

import kotlinx.serialization.Serializable

/**
 * Represents a single pump status change log entry.
 */
@Serializable
data class PumpLogEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val pumpStatus: Boolean, // true = ON, false = OFF
    val soilMoisture: Int? = null,
    val threshold: Int? = null,
    val mode: Boolean? = null // false = auto, true = manual
)
