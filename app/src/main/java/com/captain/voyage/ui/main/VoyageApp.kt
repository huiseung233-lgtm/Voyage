package com.captain.voyage.ui.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.captain.voyage.ui.goals.GoalsScreen
import com.captain.voyage.ui.home.HomeScreen
import com.captain.voyage.ui.home.HomeViewModel
import com.captain.voyage.ui.rules.RulesScreen
import com.captain.voyage.ui.rules.RulesViewModel
import com.captain.voyage.ui.settings.SettingsScreen
import com.captain.voyage.ui.settings.SettingsViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "선장실", Icons.Default.Home)
    object Rules : Screen("rules", "규율", Icons.AutoMirrored.Filled.List)
    object Goals : Screen("goals", "항로", Icons.Default.Star)
    object Settings : Screen("settings", "조타실", Icons.Default.Settings)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VoyageApp() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Rules, Screen.Goals, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                // Hilt를 사용하여 ViewModel 주입
                val viewModel = hiltViewModel<HomeViewModel>()
                HomeScreen(viewModel = viewModel)
            }
            composable(Screen.Rules.route) {
                val viewModel = hiltViewModel<RulesViewModel>()
                RulesScreen(viewModel = viewModel)
            }
            composable(Screen.Goals.route) {
                GoalsScreen()
            }
            composable(Screen.Settings.route) {
                val viewModel = hiltViewModel<SettingsViewModel>()
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
