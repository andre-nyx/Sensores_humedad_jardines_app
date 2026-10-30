package com.jardines.sensores.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jardines.sensores.data.SensorRepository
import com.jardines.sensores.model.ConnectionType
import com.jardines.sensores.model.GardenSensor
import com.jardines.sensores.model.MoistureStatus
import com.jardines.sensores.model.SensorAlert
import com.jardines.sensores.model.SoilType
import com.jardines.sensores.model.WeatherForecast
import com.jardines.sensores.model.Zone
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

enum class FilterStatus {
    TODOS,
    SECO,
    OPTIMO,
    EXCESO
}

data class UiSummaryState(
    val totalSensors: Int = 0,
    val optimalCount: Int = 0,
    val needsWaterCount: Int = 0,
    val excessCount: Int = 0,
    val averageMoisture: Int = 0,
    val unreadAlertsCount: Int = 0
)

class SensorViewModel(
    private val repository: SensorRepository = SensorRepository()
) : ViewModel() {

    val sensors: StateFlow<List<GardenSensor>> = repository.sensors
    val alerts: StateFlow<List<SensorAlert>> = repository.alerts
    val weatherForecast: StateFlow<WeatherForecast> = repository.weatherForecast

    private val _selectedFilter = MutableStateFlow(FilterStatus.TODOS)
    val selectedFilter: StateFlow<FilterStatus> = _selectedFilter.asStateFlow()

    private val _selectedZone = MutableStateFlow<Zone?>(null)
    val selectedZone: StateFlow<Zone?> = _selectedZone.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSensorId = MutableStateFlow<String?>(null)
    val selectedSensorId: StateFlow<String?> = _selectedSensorId.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    // Filtered sensors
    val filteredSensors: StateFlow<List<GardenSensor>> = combine(
        sensors,
        selectedFilter,
        selectedZone,
        searchQuery
    ) { list, filter, zone, query ->
        list.filter { sensor ->
            val matchesFilter = when (filter) {
                FilterStatus.TODOS -> true
                FilterStatus.SECO -> sensor.status == MoistureStatus.SECO
                FilterStatus.OPTIMO -> sensor.status == MoistureStatus.OPTIMO
                FilterStatus.EXCESO -> sensor.status == MoistureStatus.EXCESO
            }
            val matchesZone = zone == null || sensor.zone == zone
            val matchesQuery = query.isBlank() || sensor.name.contains(query, ignoreCase = true) || sensor.zone.label.contains(query, ignoreCase = true)
            matchesFilter && matchesZone && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary statistics
    val uiSummary: StateFlow<UiSummaryState> = combine(
        sensors,
        alerts
    ) { sensorList, alertList ->
        val total = sensorList.size
        val optimal = sensorList.count { it.status == MoistureStatus.OPTIMO }
        val needsWater = sensorList.count { it.status == MoistureStatus.SECO }
        val excess = sensorList.count { it.status == MoistureStatus.EXCESO }
        val avgMoisture = if (total > 0) sensorList.map { it.moisturePercentage }.average().toInt() else 0
        val unread = alertList.count { !it.isRead }

        UiSummaryState(
            totalSensors = total,
            optimalCount = optimal,
            needsWaterCount = needsWater,
            excessCount = excess,
            averageMoisture = avgMoisture,
            unreadAlertsCount = unread
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiSummaryState())

    init {
        // Auto simulation loop to make sensors feel dynamic
        startAutoSimulation()
    }

    private fun startAutoSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(12000) // update readings every 12 seconds
                repository.simulateSensorReadingUpdate()
            }
        }
    }

    fun setFilter(filter: FilterStatus) {
        _selectedFilter.value = filter
    }

    fun setZone(zone: Zone?) {
        _selectedZone.value = if (_selectedZone.value == zone) null else zone
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectSensor(sensorId: String?) {
        _selectedSensorId.value = sensorId
    }

    fun toggleIrrigation(sensorId: String) {
        repository.toggleIrrigation(sensorId)
    }

    fun toggleAutoIrrigation(sensorId: String, enabled: Boolean) {
        repository.toggleAutoIrrigation(sensorId, enabled)
    }

    fun updateThresholds(sensorId: String, minThreshold: Int, maxThreshold: Int) {
        repository.updateThresholds(sensorId, minThreshold, maxThreshold)
    }

    fun addSensor(
        name: String,
        zone: Zone,
        soilType: SoilType,
        minMoisture: Int,
        maxMoisture: Int,
        connectionType: ConnectionType
    ) {
        repository.addSensor(
            name = name,
            zone = zone,
            soilType = soilType,
            minMoisture = minMoisture,
            maxMoisture = maxMoisture,
            connectionType = connectionType
        )
    }

    fun deleteSensor(sensorId: String) {
        if (_selectedSensorId.value == sensorId) {
            _selectedSensorId.value = null
        }
        repository.deleteSensor(sensorId)
    }

    fun refreshReadingsNow() {
        viewModelScope.launch {
            _isSimulating.value = true
            repository.simulateSensorReadingUpdate()
            delay(500)
            _isSimulating.value = false
        }
    }

    fun markAlertAsRead(alertId: String) {
        repository.markAlertAsRead(alertId)
    }

    fun clearAlerts() {
        repository.clearAllAlerts()
    }
}
