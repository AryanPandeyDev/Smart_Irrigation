package com.example.smartirrigation.data.network.dto

import kotlinx.serialization.Serializable
import java.io.Serial


@Serializable
data class Threshold (
    val threshold : Int
)

@Serializable
data class ResponseMessage(
    val status : String,
    val threshold : Int
)