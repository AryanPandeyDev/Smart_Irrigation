package com.example.smartirrigation.presentation.navigation

import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartirrigation.common.AppState
import com.example.smartirrigation.data.local.preferences.DatastoreManager
import com.example.smartirrigation.data.repositories.PreferencesRepoImpl
import com.example.smartirrigation.domain.repositories.PreferencesRepository
import com.example.smartirrigation.presentation.dashboard.screen.DashboardScreenWrapper
import com.example.smartirrigation.presentation.navigation.nestedNavigation.NestedNavigation
import com.example.smartirrigation.presentation.setup.screens.SetPreferencesScreen
import com.example.smartirrigation.presentation.setup.screens.SetupScreen
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun Navigation(
    context: Context = LocalContext.current
){
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Routes.SetUpScreen
    ) {
        composable<Routes.SetUpScreen>(
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = {300},
                    animationSpec = tween(300)
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                slideOutHorizontally (
                    targetOffsetX = {-300},
                    animationSpec = tween(300)
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
            SetupScreen (
                setUpComplete = {
                    // Wait for Datastore call to complete before navigating
                    scope.launch {
                        val preferencesRepository = PreferencesRepoImpl(DatastoreManager(context))
                        val isSetUp = withContext(Dispatchers.IO) {
                            preferencesRepository.getPlantInfo() != null
                        }
                        AppState.isSetUpCompleted = isSetUp

                        navController.navigate(
                            if (isSetUp) Routes.DashboardScreen
                            else Routes.PlantSetupScreen
                        ) {
                            popUpTo(Routes.SetUpScreen) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }
        composable<Routes.PlantSetupScreen>(
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = {300},
                    animationSpec = tween(300)
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                slideOutHorizontally (
                    targetOffsetX = {-300},
                    animationSpec = tween(300)
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
            SetPreferencesScreen {
                navController.navigate(Routes.DashboardScreen) {
                    popUpTo(Routes.PlantSetupScreen) {
                        inclusive = true
                    }
                }
            }
        }
        composable<Routes.DashboardScreen>(
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = {300},
                    animationSpec = tween(300)
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                slideOutHorizontally (
                    targetOffsetX = {-300},
                    animationSpec = tween(300)
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = {300},
                    animationSpec = tween(300)
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
            NestedNavigation (
                navigateToSetup = {
                    navController.navigate(Routes.SetUpScreen) {
                        popUpTo(Routes.DashboardScreen) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}