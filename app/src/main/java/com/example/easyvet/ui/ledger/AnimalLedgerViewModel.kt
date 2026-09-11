package com.example.easyvet.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.easyvet.EasyVetApplication
import com.example.easyvet.data.local.entity.AnimalEntity
import com.example.easyvet.data.model.HealthStatus
import com.example.easyvet.data.repository.AnimalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LedgerTypeFilter {
    ALL,
    INDIVIDUAL,
    HERD_GROUP
}

data class FilterGroup1(
    val species: String = "All",
    val healthStatus: HealthStatus? = null,
    val typeFilter: LedgerTypeFilter = LedgerTypeFilter.ALL
)

data class FilterGroup2(
    val searchQuery: String = "",
    val selectedAnimal: AnimalEntity? = null,
    val showRegisterDialog: Boolean = false
)

data class LedgerFilterParams(
    val group1: FilterGroup1,
    val group2: FilterGroup2
)

data class AnimalLedgerUiState(
    val animals: List<AnimalEntity> = emptyList(),
    val filteredAnimals: List<AnimalEntity> = emptyList(),
    val selectedSpecies: String = "All",
    val selectedHealthStatus: HealthStatus? = null,
    val selectedTypeFilter: LedgerTypeFilter = LedgerTypeFilter.ALL,
    val searchQuery: String = "",
    val selectedAnimal: AnimalEntity? = null,
    val showRegisterDialog: Boolean = false,
    val totalAnimalsCount: Int = 0,
    val sickOrQuarantinedCount: Int = 0,
    val herdGroupsCount: Int = 0
)

class AnimalLedgerViewModel(
    private val animalRepository: AnimalRepository
) : ViewModel() {

    private val speciesFilterFlow = MutableStateFlow("All")
    private val healthStatusFilterFlow = MutableStateFlow<HealthStatus?>(null)
    private val typeFilterFlow = MutableStateFlow(LedgerTypeFilter.ALL)
    private val searchQueryFlow = MutableStateFlow("")
    private val selectedAnimalFlow = MutableStateFlow<AnimalEntity?>(null)
    private val showRegisterDialogFlow = MutableStateFlow(false)

    private val filterGroup1Flow = combine(
        speciesFilterFlow,
        healthStatusFilterFlow,
        typeFilterFlow
    ) { species, status, type ->
        FilterGroup1(species, status, type)
    }

    private val filterGroup2Flow = combine(
        searchQueryFlow,
        selectedAnimalFlow,
        showRegisterDialogFlow
    ) { search, animal, showDialog ->
        FilterGroup2(search, animal, showDialog)
    }

    private val filterParamsFlow = combine(
        filterGroup1Flow,
        filterGroup2Flow
    ) { g1, g2 ->
        LedgerFilterParams(g1, g2)
    }

    val uiState: StateFlow<AnimalLedgerUiState> = combine(
        animalRepository.allAnimals,
        filterParamsFlow
    ) { animals, params ->
        val g1 = params.group1
        val g2 = params.group2

        val filtered = animals.filter { animal ->
            val matchesSpecies = g1.species == "All" || animal.species.equals(g1.species, ignoreCase = true)
            val matchesHealth = g1.healthStatus == null || animal.healthStatus == g1.healthStatus
            val matchesType = when (g1.typeFilter) {
                LedgerTypeFilter.ALL -> true
                LedgerTypeFilter.INDIVIDUAL -> !animal.isHerdGroup
                LedgerTypeFilter.HERD_GROUP -> animal.isHerdGroup
            }
            val matchesSearch = g2.searchQuery.isBlank() ||
                    animal.tagNumber.contains(g2.searchQuery, ignoreCase = true) ||
                    animal.name.contains(g2.searchQuery, ignoreCase = true) ||
                    animal.ownerName.contains(g2.searchQuery, ignoreCase = true) ||
                    animal.location.contains(g2.searchQuery, ignoreCase = true) ||
                    animal.breed.contains(g2.searchQuery, ignoreCase = true)

            matchesSpecies && matchesHealth && matchesType && matchesSearch
        }

        val total = animals.sumOf { if (it.isHerdGroup) it.herdSize else 1 }
        val sickOrQuarantined = animals.count {
            it.healthStatus == HealthStatus.SICK || it.healthStatus == HealthStatus.QUARANTINED
        }
        val herds = animals.count { it.isHerdGroup }

        AnimalLedgerUiState(
            animals = animals,
            filteredAnimals = filtered,
            selectedSpecies = g1.species,
            selectedHealthStatus = g1.healthStatus,
            selectedTypeFilter = g1.typeFilter,
            searchQuery = g2.searchQuery,
            selectedAnimal = g2.selectedAnimal,
            showRegisterDialog = g2.showRegisterDialog,
            totalAnimalsCount = total,
            sickOrQuarantinedCount = sickOrQuarantined,
            herdGroupsCount = herds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnimalLedgerUiState()
    )

    fun setSpeciesFilter(species: String) {
        speciesFilterFlow.value = species
    }

    fun setHealthStatusFilter(status: HealthStatus?) {
        healthStatusFilterFlow.value = status
    }

    fun setTypeFilter(type: LedgerTypeFilter) {
        typeFilterFlow.value = type
    }

    fun setSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    fun selectAnimal(animal: AnimalEntity?) {
        selectedAnimalFlow.value = animal
    }

    fun setShowRegisterDialog(show: Boolean) {
        showRegisterDialogFlow.value = show
    }

    fun updateAnimalHealthStatus(animal: AnimalEntity, newStatus: HealthStatus) {
        viewModelScope.launch {
            val updated = animal.copy(
                healthStatus = newStatus,
                lastCheckupDate = System.currentTimeMillis()
            )
            animalRepository.updateAnimal(updated)
            if (selectedAnimalFlow.value?.id == animal.id) {
                selectedAnimalFlow.value = updated
            }
        }
    }

    fun addVaccinationRecord(animal: AnimalEntity, record: String) {
        viewModelScope.launch {
            val updatedVaccines = animal.vaccinationHistory + record
            val updated = animal.copy(
                vaccinationHistory = updatedVaccines,
                lastCheckupDate = System.currentTimeMillis()
            )
            animalRepository.updateAnimal(updated)
            if (selectedAnimalFlow.value?.id == animal.id) {
                selectedAnimalFlow.value = updated
            }
        }
    }

    fun registerNewAnimal(
        tagNumber: String,
        name: String,
        species: String,
        breed: String,
        ageMonths: Int,
        gender: String,
        weightKg: Double,
        isHerdGroup: Boolean,
        herdSize: Int,
        ownerName: String,
        ownerContact: String,
        location: String,
        healthStatus: HealthStatus,
        initialVaccines: List<String>,
        notes: String
    ) {
        viewModelScope.launch {
            val animalId = "ANM-${(1000..9999).random()}"
            val tag = if (tagNumber.isNotBlank()) tagNumber else "EV-TAG-${(10000..99999).random()}"

            val newAnimal = AnimalEntity(
                id = animalId,
                tagNumber = tag,
                name = name.ifBlank { if (isHerdGroup) "Herd Group $tag" else "Animal $tag" },
                species = species,
                breed = breed.ifBlank { "Local Breed" },
                ageMonths = ageMonths,
                gender = gender,
                weightKg = weightKg,
                isHerdGroup = isHerdGroup,
                herdSize = herdSize,
                ownerName = ownerName.ifBlank { "Community Farmer" },
                ownerContact = ownerContact,
                location = location.ifBlank { "Central Village Sector" },
                healthStatus = healthStatus,
                vaccinationHistory = initialVaccines,
                lastCheckupDate = System.currentTimeMillis(),
                notes = notes
            )

            animalRepository.addAnimal(newAnimal)
            showRegisterDialogFlow.value = false
        }
    }

    class Factory(private val application: EasyVetApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AnimalLedgerViewModel::class.java)) {
                return AnimalLedgerViewModel(
                    animalRepository = application.animalRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
        }
    }
}
