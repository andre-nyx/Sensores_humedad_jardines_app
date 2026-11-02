package com.jardines.sensores

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
import com.jardines.sensores.ui.screens.LoginScreen
import com.jardines.sensores.ui.screens.SensorDetailScreen
import com.jardines.sensores.ui.screens.SettingsScreen
import com.jardines.sensores.ui.theme.DryWarningOrange
import com.jardines.sensores.ui.theme.GreenPrimary
import com.jardines.sensores.ui.theme.SensoresDeHumedadTheme
import com.jardines.sensores.utils.NotificationHelper
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

        // Registrar el canal de notificaciones
        NotificationHelper.createNotificationChannel(this)

        // Disparador de prueba diferido (espera 60 segundos y dispara la notificación)
        lifecycleScope.launch {
            delay(60000)
            NotificationHelper.sendHumeAlertNotification(
                context = this@MainActivity,
                title = "🚨 Alerta de Prueba de Sensores",
                message = "¡Funciona! La humedad del Jardín cayó por debajo del límite."
            )
        }

        setContent {
            SensoresDeHumedadTheme {
                // Solicitar permiso de notificaciones en Android 13+ (API 33+)
                RequestNotificationPermissionEffect()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Estado para controlar si el usuario ya inició sesión
                    var isLoggedIn by remember { mutableStateOf(false) }

                    if (!isLoggedIn) {
                        // Muestra la pantalla de acceso (Login) primero
                        LoginScreen(
                            onLoginSuccess = {
                                isLoggedIn = true // Al poner usuario "admin" y contraseña "1234", entra al sistema
                            }
                        )
                    } else {
                        // Una vez autenticado, carga toda la navegación de los sensores
                        MainAppNavHost(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

/**
 * Composable que solicita el permiso POST_NOTIFICATIONS en tiempo de ejecución para Android 13+ (API 33+).
 */
@Composable
fun RequestNotificationPermissionEffect() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val context = LocalContext.current
        var hasPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasPermission = isGranted
        }

        LaunchedEffect(Unit) {
            if (!hasPermission) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
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