package com.example.easyvet.ui.riskmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.easyvet.EasyVetApplication
import com.example.easyvet.data.local.entity.OutbreakAlertEntity
import com.example.easyvet.data.repository.RiskMapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class MapViewMode {
    HOTSPOTS,
    QUARANTINE_PERIMETERS
}

data class FilterParams(
    val speciesFilter: String = "All",
    val diseaseFilter: String = "All",
    val searchQuery: String = "",
    val selectedAlert: OutbreakAlertEntity? = null,
    val mapViewMode: MapViewMode = MapViewMode.HOTSPOTS
)

data class RiskMapUiState(
    val alerts: List<OutbreakAlertEntity> = emptyList(),
    val filteredAlerts: List<OutbreakAlertEntity> = emptyList(),
    val selectedSpeciesFilter: String = "All",
    val selectedDiseaseFilter: String = "All",
    val searchQuery: String = "",
    val selectedAlert: OutbreakAlertEntity? = null,
    val mapViewMode: MapViewMode = MapViewMode.HOTSPOTS,
    val totalActiveOutbreaks: Int = 0,
    val highRiskZonesCount: Int = 0,
    val totalActiveCases: Int = 0,
    val totalMortalities: Int = 0
)

class RiskMapViewModel(
    private val riskMapRepository: RiskMapRepository
) : ViewModel() {

    private val selectedSpeciesFilterFlow = MutableStateFlow("All")
    private val selectedDiseaseFilterFlow = MutableStateFlow("All")
    private val searchQueryFlow = MutableStateFlow("")
    private val selectedAlertFlow = MutableStateFlow<OutbreakAlertEntity?>(null)
    private val mapViewModeFlow = MutableStateFlow(MapViewMode.HOTSPOTS)

    private val filterParamsFlow = combine(
        selectedSpeciesFilterFlow,
        selectedDiseaseFilterFlow,
        searchQueryFlow,
        selectedAlertFlow,
        mapViewModeFlow
    ) { species, disease, search, alert, mode ->
        FilterParams(species, disease, search, alert, mode)
    }

    val uiState: StateFlow<RiskMapUiState> = combine(
        riskMapRepository.allAlerts,
        filterParamsFlow
    ) { alerts, params ->
        val filtered = alerts.filter { alert ->
            val matchesSpecies = params.speciesFilter == "All" || alert.affectedSpecies.contains(params.speciesFilter, ignoreCase = true)
            val matchesDisease = params.diseaseFilter == "All" || alert.diseaseName.equals(params.diseaseFilter, ignoreCase = true) || alert.diseaseName.contains(params.diseaseFilter, ignoreCase = true)
            val matchesSearch = params.searchQuery.isBlank() ||
                    alert.title.contains(params.searchQuery, ignoreCase = true) ||
                    alert.diseaseName.contains(params.searchQuery, ignoreCase = true) ||
                    alert.regionName.contains(params.searchQuery, ignoreCase = true)

            matchesSpecies && matchesDisease && matchesSearch
        }

        val totalOutbreaks = alerts.size
        val highRiskCount = alerts.count { it.riskScore >= 70 }
        val activeCases = alerts.sumOf { it.activeCases }
        val mortalities = alerts.sumOf { it.mortalityCount }

        RiskMapUiState(
            alerts = alerts,
            filteredAlerts = filtered,
            selectedSpeciesFilter = params.speciesFilter,
            selectedDiseaseFilter = params.diseaseFilter,
            searchQuery = params.searchQuery,
            selectedAlert = params.selectedAlert,
            mapViewMode = params.mapViewMode,
            totalActiveOutbreaks = totalOutbreaks,
            highRiskZonesCount = highRiskCount,
            totalActiveCases = activeCases,
            totalMortalities = mortalities
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RiskMapUiState()
    )

    fun setSpeciesFilter(species: String) {
        selectedSpeciesFilterFlow.value = species
    }

    fun setDiseaseFilter(disease: String) {
        selectedDiseaseFilterFlow.value = disease
    }

    fun setSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    fun selectAlert(alert: OutbreakAlertEntity?) {
        selectedAlertFlow.value = alert
    }

    fun setMapViewMode(mode: MapViewMode) {
        mapViewModeFlow.value = mode
    }

    fun toggleMapViewMode() {
        mapViewModeFlow.value = if (mapViewModeFlow.value == MapViewMode.HOTSPOTS) {
            MapViewMode.QUARANTINE_PERIMETERS
        } else {
            MapViewMode.HOTSPOTS
        }
    }

    class Factory(private val application: EasyVetApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RiskMapViewModel::class.java)) {
                return RiskMapViewModel(
                    riskMapRepository = application.riskMapRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
        }
    }
}
