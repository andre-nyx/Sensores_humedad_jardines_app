package com.jardines.sensores.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jardines.sensores.model.Zone
import com.jardines.sensores.ui.components.SensorCard
import com.jardines.sensores.ui.components.StatCard
import com.jardines.sensores.ui.theme.DryWarningOrange
import com.jardines.sensores.ui.theme.GreenPrimary
import com.jardines.sensores.ui.theme.MoistureBlue
import com.jardines.sensores.viewmodel.FilterStatus
import com.jardines.sensores.viewmodel.SensorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SensorViewModel,
    onSensorClick: (String) -> Unit,
    onNavigateToAlerts: () -> Unit,
    onAddSensorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sensors by viewModel.filteredSensors.collectAsState()
    val summary by viewModel.uiSummary.collectAsState()
    val weather by viewModel.weatherForecast.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedZone by viewModel.selectedZone.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSimulating by viewModel.isSimulating.collectAsState()

    var isSearchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Sensores de Humedad",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Jardines Inteligentes",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshReadingsNow() }) {
                        if (isSimulating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar Lecturas",
                                tint = Color.White
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = onNavigateToAlerts) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alertas",
                                tint = Color.White
                            )
                        }
                        if (summary.unreadAlertsCount > 0) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(top = 6.dp, end = 6.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(DryWarningOrange)
                                    .align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = "${summary.unreadAlertsCount}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSensorClick,
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Sensor"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier.fillMaxSize()
        ) {
            // Weather Forecast Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GreenPrimary.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Thunderstorm,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Pronóstico del Tiempo",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${weather.temperature}°C • ${weather.rainChancePercentage}% lluvia",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GreenPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = weather.recommendation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Summary Stats Grid (2x2)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = "Sensores Activos",
                            value = "${summary.totalSensors}",
                            subtitle = "Monitoreando jardín",
                            icon = Icons.Default.Yard,
                            iconBgColor = GreenPrimary.copy(alpha = 0.15f),
                            iconTint = GreenPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            title = "Requieren Riego",
                            value = "${summary.needsWaterCount}",
                            subtitle = "Humedad baja",
                            icon = Icons.Default.Warning,
                            iconBgColor = DryWarningOrange.copy(alpha = 0.15f),
                            iconTint = DryWarningOrange,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setFilter(FilterStatus.SECO) }
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = "Estado Óptimo",
                            value = "${summary.optimalCount}",
                            subtitle = "Nivel adecuado",
                            icon = Icons.Default.WaterDrop,
                            iconBgColor = Color(0xFFE8F5E9),
                            iconTint = GreenPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setFilter(FilterStatus.OPTIMO) }
                        )

                        StatCard(
                            title = "Humedad Promedio",
                            value = "${summary.averageMoisture}%",
                            subtitle = "Suelo de jardín",
                            icon = Icons.Default.Opacity,
                            iconBgColor = MoistureBlue.copy(alpha = 0.15f),
                            iconTint = MoistureBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Buscar sensor por nombre o zona...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar"
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    // Status Filters
                    Text(
                        text = "Filtrar por Estado de Humedad",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == FilterStatus.TODOS,
                                onClick = { viewModel.setFilter(FilterStatus.TODOS) },
                                label = { Text("Todos (${summary.totalSensors})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == FilterStatus.SECO,
                                onClick = { viewModel.setFilter(FilterStatus.SECO) },
                                label = { Text("🌵 Secos (${summary.needsWaterCount})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DryWarningOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == FilterStatus.OPTIMO,
                                onClick = { viewModel.setFilter(FilterStatus.OPTIMO) },
                                label = { Text("🌱 Óptimos (${summary.optimalCount})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == FilterStatus.EXCESO,
                                onClick = { viewModel.setFilter(FilterStatus.EXCESO) },
                                label = { Text("💧 Exceso (${summary.excessCount})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MoistureBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Zone Filter Chips
                    Text(
                        text = "Filtrar por Zona",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedZone == null,
                                onClick = { viewModel.setZone(null) },
                                label = { Text("Todas las Zonas") }
                            )
                        }
                        items(Zone.values()) { zone ->
                            FilterChip(
                                selected = selectedZone == zone,
                                onClick = { viewModel.setZone(zone) },
                                label = { Text(zone.label) }
                            )
                        }
                    }
                }
            }

            // Sensors List Section
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sensores de Humedad (${sensors.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (sensors.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No se encontraron sensores con este filtro.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(sensors, key = { it.id }) { sensor ->
                    SensorCard(
                        sensor = sensor,
                        onSensorClick = onSensorClick,
                        onToggleIrrigation = { viewModel.toggleIrrigation(it) }
                    )
                }
            }
        }
    }
}
