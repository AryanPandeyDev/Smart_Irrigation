package com.example.smartirrigation.presentation.navigation.nestedNavigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import com.example.smartirrigation.presentation.navigation.nestedNavigation.BottomNavigationItem
import kotlinx.serialization.Serializable

sealed class BottomNavigationRoute() {
    @Serializable
    data object Dashboard : BottomNavigationRoute()
    @Serializable
    data object ChatBot : BottomNavigationRoute()
    @Serializable
    data object Settings : BottomNavigationRoute()
}