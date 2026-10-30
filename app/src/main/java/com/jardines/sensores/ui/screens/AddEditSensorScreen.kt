package com.jardines.sensores.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jardines.sensores.model.ConnectionType
import com.jardines.sensores.model.SoilType
import com.jardines.sensores.model.Zone
import com.jardines.sensores.ui.theme.GreenPrimary
import com.jardines.sensores.ui.theme.StatusExcesoColor
import com.jardines.sensores.ui.theme.StatusSecoColor
import com.jardines.sensores.viewmodel.SensorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSensorScreen(
    viewModel: SensorViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var selectedZone by remember { mutableStateOf(Zone.JARDIN_PRINCIPAL) }
    var selectedSoil by remember { mutableStateOf(SoilType.ORGANICO) }
    var selectedConnection by remember { mutableStateOf(ConnectionType.WIFI) }
    var minMoisture by remember { mutableFloatStateOf(30f) }
    var maxMoisture by remember { mutableFloatStateOf(75f) }

    var zoneExpanded by remember { mutableStateOf(false) }
    var soilExpanded by remember { mutableStateOf(false) }
    var connExpanded by remember { mutableStateOf(false) }

    var isError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vinculación de Nuevo Sensor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 32.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier.fillMaxSize()
        ) {
            // Sensor Name
            item {
                Column {
                    Text(
                        text = "Nombre del Sensor / Ubicación",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            isError = false
                        },
                        placeholder = { Text("Ej. Macetas de Rosas, Huerto Sur, Cítricos") },
                        isError = isError,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isError) {
                        Text(
                            text = "Por favor ingresa un nombre para el sensor.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Zone Selection Dropdown
            item {
                Column {
                    Text(
                        text = "Zona del Jardín",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = zoneExpanded,
                        onExpandedChange = { zoneExpanded = !zoneExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedZone.label,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = zoneExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = zoneExpanded,
                            onDismissRequest = { zoneExpanded = false }
                        ) {
                            Zone.values().forEach { zone ->
                                DropdownMenuItem(
                                    text = { Text(zone.label) },
                                    onClick = {
                                        selectedZone = zone
                                        zoneExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Soil Type Dropdown
            item {
                Column {
                    Text(
                        text = "Tipo de Suelo / Sustrato",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = soilExpanded,
                        onExpandedChange = { soilExpanded = !soilExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSoil.label,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = soilExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = soilExpanded,
                            onDismissRequest = { soilExpanded = false }
                        ) {
                            SoilType.values().forEach { soil ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(soil.label, fontWeight = FontWeight.Bold)
                                            Text(soil.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedSoil = soil
                                        soilExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Connection Type Dropdown
            item {
                Column {
                    Text(
                        text = "Tipo de Conexión IoT",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = connExpanded,
                        onExpandedChange = { connExpanded = !connExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedConnection.label,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = connExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = connExpanded,
                            onDismissRequest = { connExpanded = false }
                        ) {
                            ConnectionType.values().forEach { conn ->
                                DropdownMenuItem(
                                    text = { Text(conn.label) },
                                    onClick = {
                                        selectedConnection = conn
                                        connExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Sliders for Min and Max Thresholds
            item {
                Column {
                    Text(
                        text = "Umbrales Deseados de Humedad",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mínima antes de regar:", fontSize = 12.sp)
                        Text("${minMoisture.toInt()}%", fontWeight = FontWeight.Bold, color = StatusSecoColor)
                    }
                    Slider(
                        value = minMoisture,
                        onValueChange = { minMoisture = it },
                        valueRange = 10f..50f,
                        colors = SliderDefaults.colors(
                            thumbColor = StatusSecoColor,
                            activeTrackColor = StatusSecoColor
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Máxima para detener riego:", fontSize = 12.sp)
                        Text("${maxMoisture.toInt()}%", fontWeight = FontWeight.Bold, color = StatusExcesoColor)
                    }
                    Slider(
                        value = maxMoisture,
                        onValueChange = { maxMoisture = it },
                        valueRange = 55f..90f,
                        colors = SliderDefaults.colors(
                            thumbColor = StatusExcesoColor,
                            activeTrackColor = StatusExcesoColor
                        )
                    )
                }
            }

            // Submit Button
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            isError = true
                        } else {
                            viewModel.addSensor(
                                name = name.trim(),
                                zone = selectedZone,
                                soilType = selectedSoil,
                                minMoisture = minMoisture.toInt(),
                                maxMoisture = maxMoisture.toInt(),
                                connectionType = selectedConnection
                            )
                            onBackClick()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(
                        text = "Guardar y Vincular Sensor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
