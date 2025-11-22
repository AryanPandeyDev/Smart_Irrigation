package com.example.smartirrigation.presentation.navigation.nestedNavigation

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavigationItem(
    val title: String,
    val selectedIcon : ImageVector,
    val unselectedIcon : ImageVector,
    val route: BottomNavigationRoute
)