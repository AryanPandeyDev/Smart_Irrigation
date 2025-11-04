package com.example.smartirrigation.presentation.navigation.nestedNavigation

import android.annotation.SuppressLint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.smartirrigation.presentation.chatbot.screens.ChatbotScreen
import com.example.smartirrigation.presentation.dashboard.screen.DashboardScreenWrapper
import com.example.smartirrigation.presentation.settings.screens.SettingsScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NestedNavigation(
    navigateToSetup : () -> Unit
) {
    val navController = rememberNavController()
    val color = MaterialTheme.colorScheme.outline
    val items = listOf(
        BottomNavigationItem(
            title = "Dashboard",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            route = BottomNavigationRoute.Dashboard
        ),
        BottomNavigationItem(
            title = "ChatBot",
            selectedIcon = Icons.Filled.SmartToy,
            unselectedIcon = Icons.Outlined.SmartToy,
            route = BottomNavigationRoute.ChatBot
        ),
        BottomNavigationItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            route = BottomNavigationRoute.Settings
        )
    )
    var selectedItem by rememberSaveable {
        mutableIntStateOf(0)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.fillMaxWidth()
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx() // line thickness
                        drawLine(
                            color = color,
                            start = Offset(0f, 0f),               // start at top-left
                            end = Offset(size.width, 0f),         // end at top-right
                            strokeWidth = strokeWidth
                        )
                    }
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = {
                            if (selectedItem != index) {
                                selectedItem = index
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            }
                        },
                        icon = {
                            Icon(if (selectedItem == index) {
                                item.selectedIcon
                            } else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(text = item.title)
                        }
                    )
                }
            }
        },
        content = {
            NavHost(
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                startDestination = BottomNavigationRoute.Dashboard
            ) {
                composable<BottomNavigationRoute.Dashboard>(
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
                            initialOffsetX = {-500},
                            animationSpec = tween(300)
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                ) {
                    DashboardScreenWrapper(
                        navigateToSetupScreen = {
                            navigateToSetup()
                        }
                    )
                }
                composable<BottomNavigationRoute.ChatBot>(
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
                    ChatbotScreen()
                }
                composable<BottomNavigationRoute.Settings>(
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
                ) {
                    SettingsScreen()
                }
            }
        }
    )
}