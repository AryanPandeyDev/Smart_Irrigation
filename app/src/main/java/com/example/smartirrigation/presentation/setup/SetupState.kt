package com.example.smartirrigation.presentation.setup

data class SetupState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)