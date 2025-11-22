package com.example.smartirrigation.presentation.navigation

import kotlinx.serialization.Serializable


@Serializable
sealed interface Routes {
    @Serializable
    data object SetUpScreen : Routes
    @Serializable
    data object PlantSetupScreen : Routes

    @Serializable
    data object DashboardScreen : Routes
}