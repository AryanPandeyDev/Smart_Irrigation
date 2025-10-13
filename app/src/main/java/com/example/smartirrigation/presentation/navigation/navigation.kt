package com.example.smartirrigation.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartirrigation.presentation.setup.screens.SetPreferencesScreen
import com.example.smartirrigation.presentation.setup.screens.SetupScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SetUpScreen
    ) {
        composable<Routes.SetUpScreen> {
            SetupScreen {
                navController.navigate(Routes.PlantSetupScreen)
            }
        }
        composable<Routes.PlantSetupScreen> {
            SetPreferencesScreen {

            }
        }
    }
}