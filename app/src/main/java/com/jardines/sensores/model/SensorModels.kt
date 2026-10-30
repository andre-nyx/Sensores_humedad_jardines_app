package com.jardines.sensores.model

enum class MoistureStatus(val label: String) {
    SECO("Baja Humedad (Requiere Riego)"),
    OPTIMO("Humedad Óptima"),
    EXCESO("Exceso de Agua")
}

enum class SoilType(val label: String, val description: String) {
    ORGANICO("Sustrato Orgánico", "Excelente retención de humedad y nutrientes"),
    UNIVERSAL("Tierra Universal", "Retención media de humedad"),
    ARCILLOSO("Suelo Arcilloso", "Retiene mucho tiempo la humedad"),
    ARENOSO("Suelo Arenoso", "Drenaje rápido, requiere riegos breves")
}

enum class Zone(val label: String, val iconName: String) {
    JARDIN_PRINCIPAL("Jardín Principal", "Grass"),
    HUERTO("Huerto Orgánico", "Agriculture"),
    BALCON("Balcón / Macetas", "LocalFlorist"),
    INVERNADERO("Invernadero", "Yard"),
    CESPED("Césped Delantero", "Park")
}

enum class ConnectionType(val label: String) {
    WIFI("Wi-Fi Smart"),
    BLUETOOTH("Bluetooth Low Energy"),
    ZIGBEE("Zigbee Mesh")
}

enum class AlertSeverity {
    CRITICAL,
    WARNING,
    INFO
}

data class HistoricalPoint(
    val timeLabel: String,
    val moistureValue: Int
)

data class GardenSensor(
    val id: String,
    val name: String,
    val zone: Zone,
    val soilType: SoilType,
    val moisturePercentage: Int,
    val temperatureCelsius: Double,
    val ambientHumidityPercentage: Int,
    val sunlightLux: Int,
    val batteryPercentage: Int,
    val signalStrengthDbm: Int,
    val minMoistureThreshold: Int = 30,
    val maxMoistureThreshold: Int = 75,
    val isAutoIrrigationEnabled: Boolean = true,
    val isIrrigatingNow: Boolean = false,
    val lastUpdated: String,
    val connectionType: ConnectionType = ConnectionType.WIFI,
    val historicalMoisture: List<HistoricalPoint> = emptyList()
) {
    val status: MoistureStatus
        get() = when {
            moisturePercentage < minMoistureThreshold -> MoistureStatus.SECO
            moisturePercentage > maxMoistureThreshold -> MoistureStatus.EXCESO
            else -> MoistureStatus.OPTIMO
        }
}

data class SensorAlert(
    val id: String,
    val sensorId: String,
    val sensorName: String,
    val message: String,
    val timestamp: String,
    val severity: AlertSeverity,
    val isRead: Boolean = false
)

data class WeatherForecast(
    val condition: String,
    val temperature: Double,
    val rainChancePercentage: Int,
    val recommendation: String
)
