package com.example.smartirrigation.presentation.setup.state

data class SetupState (
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class PlantSetupUiState(
    val plantType: String = "",
    val threshold: String = "",
    val errors: PlantErrors = PlantErrors(),
    val isEnabled : Boolean = false
)

data class PlantErrors(
    val plantError : String? = null,
    val thresholdEmpty : String? = null,
    val thresholdInvalid : String? = null,
)