package com.jardines.sensores

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jardines.sensores.ui.screens.AddEditSensorScreen
import com.jardines.sensores.ui.screens.AlertsScreen
import com.jardines.sensores.ui.screens.DashboardScreen
import com.jardines.sensores.ui.screens.SensorDetailScreen
import com.jardines.sensores.ui.screens.SettingsScreen
import com.jardines.sensores.ui.theme.DryWarningOrange
import com.jardines.sensores.ui.theme.GreenPrimary
import com.jardines.sensores.ui.theme.SensoresDeHumedadTheme
import com.jardines.sensores.viewmodel.SensorViewModel

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Inicio : BottomNavItem("dashboard", "Inicio", Icons.Default.Yard)
    object Alertas : BottomNavItem("alerts", "Alertas", Icons.Default.Notifications)
    object Ajustes : BottomNavItem("settings", "Ajustes", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: SensorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SensoresDeHumedadTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppNavHost(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppNavHost(viewModel: SensorViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val summary by viewModel.uiSummary.collectAsState()

    val bottomNavItems = listOf(
        BottomNavItem.Inicio,
        BottomNavItem.Alertas,
        BottomNavItem.Ajustes
    )

    val showBottomBar = currentRoute in listOf(
        BottomNavItem.Inicio.route,
        BottomNavItem.Alertas.route,
        BottomNavItem.Ajustes.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = GreenPrimary
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (item == BottomNavItem.Alertas && summary.unreadAlertsCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = DryWarningOrange) {
                                                Text("${summary.unreadAlertsCount}")
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = item.icon, contentDescription = item.title)
                                    }
                                } else {
                                    Icon(imageVector = item.icon, contentDescription = item.title)
                                }
                            },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GreenPrimary,
                                selectedTextColor = GreenPrimary,
                                indicatorColor = GreenPrimary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Inicio.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Inicio.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onSensorClick = { sensorId ->
                        navController.navigate("sensorDetail/$sensorId")
                    },
                    onNavigateToAlerts = {
                        navController.navigate(BottomNavItem.Alertas.route)
                    },
                    onAddSensorClick = {
                        navController.navigate("addSensor")
                    }
                )
            }

            composable(
                route = "sensorDetail/{sensorId}",
                arguments = listOf(navArgument("sensorId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sensorId = backStackEntry.arguments?.getString("sensorId") ?: ""
                SensorDetailScreen(
                    sensorId = sensorId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(BottomNavItem.Alertas.route) {
                AlertsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("addSensor") {
                AddEditSensorScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(BottomNavItem.Ajustes.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
