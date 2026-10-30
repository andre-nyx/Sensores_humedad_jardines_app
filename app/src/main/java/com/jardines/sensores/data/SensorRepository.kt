package com.jardines.sensores.data

import com.jardines.sensores.model.AlertSeverity
import com.jardines.sensores.model.ConnectionType
import com.jardines.sensores.model.GardenSensor
import com.jardines.sensores.model.HistoricalPoint
import com.jardines.sensores.model.SensorAlert
import com.jardines.sensores.model.SoilType
import com.jardines.sensores.model.WeatherForecast
import com.jardines.sensores.model.Zone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SensorRepository {

    private val _sensors = MutableStateFlow<List<GardenSensor>>(getInitialSensors())
    val sensors: StateFlow<List<GardenSensor>> = _sensors.asStateFlow()

    private val _alerts = MutableStateFlow<List<SensorAlert>>(getInitialAlerts())
    val alerts: StateFlow<List<SensorAlert>> = _alerts.asStateFlow()

    private val _weatherForecast = MutableStateFlow(
        WeatherForecast(
            condition = "Parcialmente Nublado",
            temperature = 22.5,
            rainChancePercentage = 65,
            recommendation = "Probabilidad alta de lluvia por la tarde (65%). El riego automático ha pausado 2 zonas para conservar agua."
        )
    )
    val weatherForecast: StateFlow<WeatherForecast> = _weatherForecast.asStateFlow()

    fun toggleIrrigation(sensorId: String) {
        _sensors.update { list ->
            list.map { sensor ->
                if (sensor.id == sensorId) {
                    val newIrrigatingState = !sensor.isIrrigatingNow
                    val updatedMoisture = if (newIrrigatingState) {
                        (sensor.moisturePercentage + 15).coerceAtMost(100)
                    } else {
                        sensor.moisturePercentage
                    }

                    if (newIrrigatingState) {
                        addAlert(
                            sensorId = sensor.id,
                            sensorName = sensor.name,
                            message = "Riego manual iniciado para ${sensor.name}.",
                            severity = AlertSeverity.INFO
                        )
                    }

                    sensor.copy(
                        isIrrigatingNow = newIrrigatingState,
                        moisturePercentage = updatedMoisture,
                        lastUpdated = getCurrentTimeString()
                    )
                } else {
                    sensor
                }
            }
        }
    }

    fun toggleAutoIrrigation(sensorId: String, enabled: Boolean) {
        _sensors.update { list ->
            list.map { sensor ->
                if (sensor.id == sensorId) {
                    sensor.copy(isAutoIrrigationEnabled = enabled)
                } else {
                    sensor
                }
            }
        }
    }

    fun updateThresholds(sensorId: String, minThreshold: Int, maxThreshold: Int) {
        _sensors.update { list ->
            list.map { sensor ->
                if (sensor.id == sensorId) {
                    sensor.copy(
                        minMoistureThreshold = minThreshold,
                        maxMoistureThreshold = maxThreshold
                    )
                } else {
                    sensor
                }
            }
        }
    }

    fun addSensor(
        name: String,
        zone: Zone,
        soilType: SoilType,
        minMoisture: Int,
        maxMoisture: Int,
        connectionType: ConnectionType
    ) {
        val newSensor = GardenSensor(
            id = UUID.randomUUID().toString(),
            name = name,
            zone = zone,
            soilType = soilType,
            moisturePercentage = 50,
            temperatureCelsius = 21.0,
            ambientHumidityPercentage = 55,
            sunlightLux = 4200,
            batteryPercentage = 100,
            signalStrengthDbm = -55,
            minMoistureThreshold = minMoisture,
            maxMoistureThreshold = maxMoisture,
            isAutoIrrigationEnabled = true,
            isIrrigatingNow = false,
            lastUpdated = getCurrentTimeString(),
            connectionType = connectionType,
            historicalMoisture = listOf(
                HistoricalPoint("08:00", 45),
                HistoricalPoint("10:00", 48),
                HistoricalPoint("12:00", 50)
            )
        )

        _sensors.update { it + newSensor }
        addAlert(
            sensorId = newSensor.id,
            sensorName = newSensor.name,
            message = "Nuevo sensor '${newSensor.name}' vinculado correctamente.",
            severity = AlertSeverity.INFO
        )
    }

    fun deleteSensor(sensorId: String) {
        _sensors.update { list -> list.filterNot { it.id == sensorId } }
    }

    fun simulateSensorReadingUpdate() {
        _sensors.update { list ->
            list.map { sensor ->
                // Simulate minor fluctuation or moisture drop/increase
                val delta = if (sensor.isIrrigatingNow) +5 else (-3..2).random()
                val newMoisture = (sensor.moisturePercentage + delta).coerceIn(5, 100)
                
                // Check if low moisture warning should be emitted
                if (newMoisture < sensor.minMoistureThreshold && sensor.moisturePercentage >= sensor.minMoistureThreshold) {
                    addAlert(
                        sensorId = sensor.id,
                        sensorName = sensor.name,
                        message = "¡Atención! La humedad en '${sensor.name}' bajó a $newMoisture% (Mínimo: ${sensor.minMoistureThreshold}%).",
                        severity = AlertSeverity.WARNING
                    )
                }

                val currentHistory = sensor.historicalMoisture.toMutableList()
                if (currentHistory.size >= 8) {
                    currentHistory.removeAt(0)
                }
                currentHistory.add(HistoricalPoint(getCurrentTimeString().takeLast(5), newMoisture))

                sensor.copy(
                    moisturePercentage = newMoisture,
                    lastUpdated = getCurrentTimeString(),
                    historicalMoisture = currentHistory
                )
            }
        }
    }

    fun markAlertAsRead(alertId: String) {
        _alerts.update { list ->
            list.map { alert ->
                if (alert.id == alertId) alert.copy(isRead = true) else alert
            }
        }
    }

    fun clearAllAlerts() {
        _alerts.value = emptyList()
    }

    private fun addAlert(sensorId: String, sensorName: String, message: String, severity: AlertSeverity) {
        val newAlert = SensorAlert(
            id = UUID.randomUUID().toString(),
            sensorId = sensorId,
            sensorName = sensorName,
            message = message,
            timestamp = getCurrentTimeString(),
            severity = severity,
            isRead = false
        )
        _alerts.update { listOf(newAlert) + it }
    }

    private fun getCurrentTimeString(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun getInitialSensors(): List<GardenSensor> {
        return listOf(
            GardenSensor(
                id = "sensor-1",
                name = "Huerto de Tomates",
                zone = Zone.HUERTO,
                soilType = SoilType.ORGANICO,
                moisturePercentage = 22,
                temperatureCelsius = 24.5,
                ambientHumidityPercentage = 48,
                sunlightLux = 6500,
                batteryPercentage = 88,
                signalStrengthDbm = -62,
                minMoistureThreshold = 35,
                maxMoistureThreshold = 70,
                isAutoIrrigationEnabled = true,
                isIrrigatingNow = false,
                lastUpdated = "10:15 AM",
                connectionType = ConnectionType.WIFI,
                historicalMoisture = listOf(
                    HistoricalPoint("04:00", 42),
                    HistoricalPoint("06:00", 38),
                    HistoricalPoint("08:00", 30),
                    HistoricalPoint("10:00", 24),
                    HistoricalPoint("10:15", 22)
                )
            ),
            GardenSensor(
                id = "sensor-2",
                name = "Rosas del Jardín Principal",
                zone = Zone.JARDIN_PRINCIPAL,
                soilType = SoilType.UNIVERSAL,
                moisturePercentage = 58,
                temperatureCelsius = 21.8,
                ambientHumidityPercentage = 55,
                sunlightLux = 5200,
                batteryPercentage = 95,
                signalStrengthDbm = -58,
                minMoistureThreshold = 30,
                maxMoistureThreshold = 75,
                isAutoIrrigationEnabled = true,
                isIrrigatingNow = false,
                lastUpdated = "10:20 AM",
                connectionType = ConnectionType.ZIGBEE,
                historicalMoisture = listOf(
                    HistoricalPoint("04:00", 62),
                    HistoricalPoint("06:00", 60),
                    HistoricalPoint("08:00", 59),
                    HistoricalPoint("10:00", 58)
                )
            ),
            GardenSensor(
                id = "sensor-3",
                name = "Macetas de Albahaca y Menta",
                zone = Zone.BALCON,
                soilType = SoilType.ORGANICO,
                moisturePercentage = 82,
                temperatureCelsius = 23.0,
                ambientHumidityPercentage = 60,
                sunlightLux = 3800,
                batteryPercentage = 74,
                signalStrengthDbm = -70,
                minMoistureThreshold = 40,
                maxMoistureThreshold = 80,
                isAutoIrrigationEnabled = false,
                isIrrigatingNow = false,
                lastUpdated = "10:18 AM",
                connectionType = ConnectionType.BLUETOOTH,
                historicalMoisture = listOf(
                    HistoricalPoint("04:00", 50),
                    HistoricalPoint("06:00", 50),
                    HistoricalPoint("08:00", 85),
                    HistoricalPoint("10:00", 82)
                )
            ),
            GardenSensor(
                id = "sensor-4",
                name = "Invernadero de Orquídeas",
                zone = Zone.INVERNADERO,
                soilType = SoilType.ORGANICO,
                moisturePercentage = 48,
                temperatureCelsius = 26.2,
                ambientHumidityPercentage = 72,
                sunlightLux = 2900,
                batteryPercentage = 91,
                signalStrengthDbm = -52,
                minMoistureThreshold = 35,
                maxMoistureThreshold = 65,
                isAutoIrrigationEnabled = true,
                isIrrigatingNow = false,
                lastUpdated = "10:22 AM",
                connectionType = ConnectionType.WIFI,
                historicalMoisture = listOf(
                    HistoricalPoint("04:00", 52),
                    HistoricalPoint("06:00", 51),
                    HistoricalPoint("08:00", 49),
                    HistoricalPoint("10:00", 48)
                )
            ),
            GardenSensor(
                id = "sensor-5",
                name = "Césped Central",
                zone = Zone.CESPED,
                soilType = SoilType.ARCILLOSO,
                moisturePercentage = 18,
                temperatureCelsius = 25.1,
                ambientHumidityPercentage = 42,
                sunlightLux = 8100,
                batteryPercentage = 15,
                signalStrengthDbm = -78,
                minMoistureThreshold = 25,
                maxMoistureThreshold = 70,
                isAutoIrrigationEnabled = true,
                isIrrigatingNow = false,
                lastUpdated = "09:55 AM",
                connectionType = ConnectionType.WIFI,
                historicalMoisture = listOf(
                    HistoricalPoint("04:00", 35),
                    HistoricalPoint("06:00", 28),
                    HistoricalPoint("08:00", 22),
                    HistoricalPoint("09:55", 18)
                )
            )
        )
    }

    private fun getInitialAlerts(): List<SensorAlert> {
        return listOf(
            SensorAlert(
                id = "alert-1",
                sensorId = "sensor-5",
                sensorName = "Césped Central",
                message = "Humedad crítica (18%). Se recomienda activar riego urgente.",
                timestamp = "09:55 AM",
                severity = AlertSeverity.CRITICAL,
                isRead = false
            ),
            SensorAlert(
                id = "alert-2",
                sensorId = "sensor-5",
                sensorName = "Césped Central",
                message = "Nivel de batería bajo (15%). Considere recargar la batería solar.",
                timestamp = "09:50 AM",
                severity = AlertSeverity.WARNING,
                isRead = false
            ),
            SensorAlert(
                id = "alert-3",
                sensorId = "sensor-1",
                sensorName = "Huerto de Tomates",
                message = "Nivel de humedad por debajo del umbral óptimo (22%).",
                timestamp = "10:15 AM",
                severity = AlertSeverity.WARNING,
                isRead = false
            ),
            SensorAlert(
                id = "alert-4",
                sensorId = "sensor-3",
                sensorName = "Macetas de Albahaca y Menta",
                message = "Exceso de agua detectado (82%). El riego automático ha sido pausado.",
                timestamp = "08:10 AM",
                severity = AlertSeverity.INFO,
                isRead = true
            )
        )
    }
}
