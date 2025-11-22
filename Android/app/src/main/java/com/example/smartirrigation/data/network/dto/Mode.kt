package com.example.smartirrigation.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class Mode(
    val mode: Boolean
)

@Serializable
data class ModeResponse(
    val status: String,
    val mode: Boolean
)