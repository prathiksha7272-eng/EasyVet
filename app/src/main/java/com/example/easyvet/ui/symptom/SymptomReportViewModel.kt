package com.example.easyvet.ui.symptom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.easyvet.EasyVetApplication
import com.example.easyvet.data.local.entity.AnimalEntity
import com.example.easyvet.data.local.entity.SymptomReportEntity
import com.example.easyvet.data.model.Species
import com.example.easyvet.data.model.Symptom
import com.example.easyvet.data.model.TriageResult
import com.example.easyvet.data.repository.AnimalRepository
import com.example.easyvet.data.repository.SymptomReportRepository
import com.example.easyvet.data.triage.TriageEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SymptomReportFormState(
    val species: Species = Species.CATTLE,
    val earTagOrHerdId: String = "",
    val selectedAnimal: AnimalEntity? = null,
    val selectedSymptoms: Set<Symptom> = emptySet(),
    val affectedCount: Int = 1,
    val totalHerdCount: Int = 1,
    val mortalityCount: Int = 0,
    val symptomOnsetDays: Int = 1,
    val locationName: String = "Kibaha Ward 3, Coast Region",
    val latitude: Double = -6.7783,
    val longitude: Double = 38.9221,
    val reporterName: String = "Field Paravet",
    val reporterRole: String = "Community Health Worker",
    val imageUris: List<String> = emptyList(),
    val notes: String = "",
    val liveTriageResult: TriageResult = TriageEngine.evaluate(emptyList(), Species.CATTLE),
    val isSubmitting: Boolean = false,
    val submittedReport: SymptomReportEntity? = null,
    val submittedTriageResult: TriageResult? = null,
    val showTriageDialog: Boolean = false
)

private data class ClinicalData(
    val species: Species,
    val earTag: String,
    val animal: AnimalEntity?,
    val symptoms: Set<Symptom>
)

private data class NumbersData(
    val affected: Int,
    val total: Int,
    val mortality: Int,
    val onsetDays: Int
)

private data class LocationReporterData(
    val location: String,
    val lat: Double,
    val lng: Double,
    val reporterName: String,
    val reporterRole: String,
    val images: List<String>,
    val notes: String
)

private data class SubmissionData(
    val isSubmitting: Boolean,
    val submittedReport: SymptomReportEntity?,
    val submittedTriageResult: TriageResult?,
    val showTriageDialog: Boolean
)

class SymptomReportViewModel(
    private val symptomReportRepository: SymptomReportRepository,
    private val animalRepository: AnimalRepository
) : ViewModel() {

    private val _species = MutableStateFlow(Species.CATTLE)
    private val _earTagOrHerdId = MutableStateFlow("")
    private val _selectedAnimal = MutableStateFlow<AnimalEntity?>(null)
    private val _selectedSymptoms = MutableStateFlow<Set<Symptom>>(emptySet())
    private val _affectedCount = MutableStateFlow(1)
    private val _totalHerdCount = MutableStateFlow(1)
    private val _mortalityCount = MutableStateFlow(0)
    private val _symptomOnsetDays = MutableStateFlow(1)
    private val _locationName = MutableStateFlow("Kibaha Ward 3, Coast Region")
    private val _latitude = MutableStateFlow(-6.7783)
    private val _longitude = MutableStateFlow(38.9221)
    private val _reporterName = MutableStateFlow("Field Paravet")
    private val _reporterRole = MutableStateFlow("Community Health Worker")
    private val _imageUris = MutableStateFlow<List<String>>(emptyList())
    private val _notes = MutableStateFlow("")
    private val _isSubmitting = MutableStateFlow(false)
    private val _submittedReport = MutableStateFlow<SymptomReportEntity?>(null)
    private val _submittedTriageResult = MutableStateFlow<TriageResult?>(null)
    private val _showTriageDialog = MutableStateFlow(false)

    val registeredAnimals: StateFlow<List<AnimalEntity>> = animalRepository.allAnimals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val clinicalData = combine(_species, _earTagOrHerdId, _selectedAnimal, _selectedSymptoms) { species, tag, animal, symptoms ->
        ClinicalData(species, tag, animal, symptoms)
    }

    private val numbersData = combine(_affectedCount, _totalHerdCount, _mortalityCount, _symptomOnsetDays) { affected, total, mortality, onset ->
        NumbersData(affected, total, mortality, onset)
    }

    private val locationReporterData = combine(
        combine(_locationName, _latitude, _longitude) { loc, lat, lng -> Triple(loc, lat, lng) },
        combine(_reporterName, _reporterRole) { name, role -> Pair(name, role) },
        combine(_imageUris, _notes) { images, notes -> Pair(images, notes) }
    ) { locLat, nameRole, imagesNotes ->
        LocationReporterData(
            location = locLat.first,
            lat = locLat.second,
            lng = locLat.third,
            reporterName = nameRole.first,
            reporterRole = nameRole.second,
            images = imagesNotes.first,
            notes = imagesNotes.second
        )
    }

    private val submissionData = combine(_isSubmitting, _submittedReport, _submittedTriageResult, _showTriageDialog) { submitting, report, triage, showDialog ->
        SubmissionData(submitting, report, triage, showDialog)
    }

    val formState: StateFlow<SymptomReportFormState> = combine(
        clinicalData,
        numbersData,
        locationReporterData,
        submissionData
    ) { clinical, numbers, locRep, sub ->
        val liveResult = TriageEngine.evaluate(
            symptoms = clinical.symptoms.toList(),
            species = clinical.species,
            affectedCount = numbers.affected,
            mortalityCount = numbers.mortality,
            totalHerdCount = numbers.total
        )

        SymptomReportFormState(
            species = clinical.species,
            earTagOrHerdId = clinical.earTag,
            selectedAnimal = clinical.animal,
            selectedSymptoms = clinical.symptoms,
            affectedCount = numbers.affected,
            totalHerdCount = numbers.total,
            mortalityCount = numbers.mortality,
            symptomOnsetDays = numbers.onsetDays,
            locationName = locRep.location,
            latitude = locRep.lat,
            longitude = locRep.lng,
            reporterName = locRep.reporterName,
            reporterRole = locRep.reporterRole,
            imageUris = locRep.images,
            notes = locRep.notes,
            liveTriageResult = liveResult,
            isSubmitting = sub.isSubmitting,
            submittedReport = sub.submittedReport,
            submittedTriageResult = sub.submittedTriageResult,
            showTriageDialog = sub.showTriageDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SymptomReportFormState()
    )

    fun updateSpecies(species: Species) {
        _species.value = species
    }

    fun updateEarTag(tag: String) {
        _earTagOrHerdId.value = tag
        val matched = registeredAnimals.value.find { it.tagNumber.equals(tag, ignoreCase = true) }
        _selectedAnimal.value = matched
    }

    fun selectAnimal(animal: AnimalEntity) {
        _selectedAnimal.value = animal
        _earTagOrHerdId.value = animal.tagNumber
        val sp = Species.entries.find { it.displayName.equals(animal.species, ignoreCase = true) }
        if (sp != null) {
            _species.value = sp
        }
        if (animal.isHerdGroup && animal.herdSize > 0) {
            _totalHerdCount.value = animal.herdSize
        }
    }

    fun toggleSymptom(symptom: Symptom) {
        val current = _selectedSymptoms.value.toMutableSet()
        if (current.contains(symptom)) {
            current.remove(symptom)
        } else {
            current.add(symptom)
        }
        _selectedSymptoms.value = current
    }

    fun updateAffectedCount(count: Int) {
        val validCount = count.coerceAtLeast(1)
        _affectedCount.value = validCount
        if (validCount > _totalHerdCount.value) {
            _totalHerdCount.value = validCount
        }
    }

    fun updateTotalHerdCount(count: Int) {
        val validCount = count.coerceAtLeast(_affectedCount.value)
        _totalHerdCount.value = validCount
    }

    fun updateMortalityCount(count: Int) {
        val validCount = count.coerceAtLeast(0)
        _mortalityCount.value = validCount
        if (validCount > _affectedCount.value) {
            _affectedCount.value = validCount
        }
    }

    fun updateOnsetDays(days: Int) {
        _symptomOnsetDays.value = days.coerceAtLeast(1)
    }

    fun updateLocation(name: String, lat: Double = -6.7783, lng: Double = 38.9221) {
        _locationName.value = name
        _latitude.value = lat
        _longitude.value = lng
    }

    fun updateReporter(name: String, role: String) {
        _reporterName.value = name
        _reporterRole.value = role
    }

    fun addImageUri(uri: String) {
        _imageUris.value = _imageUris.value + uri
    }

    fun removeImageUri(uri: String) {
        _imageUris.value = _imageUris.value - uri
    }

    fun updateNotes(notesText: String) {
        _notes.value = notesText
    }

    fun submitReport() {
        if (_isSubmitting.value) return

        viewModelScope.launch {
            _isSubmitting.value = true

            val liveTriage = TriageEngine.evaluate(
                symptoms = _selectedSymptoms.value.toList(),
                species = _species.value,
                affectedCount = _affectedCount.value,
                mortalityCount = _mortalityCount.value,
                totalHerdCount = _totalHerdCount.value
            )

            val report = symptomReportRepository.submitReport(
                animalId = _selectedAnimal.value?.id,
                species = _species.value,
                affectedCount = _affectedCount.value,
                totalHerdCount = _totalHerdCount.value,
                mortalityCount = _mortalityCount.value,
                selectedSymptoms = _selectedSymptoms.value.toList(),
                onsetDays = _symptomOnsetDays.value,
                latitude = _latitude.value,
                longitude = _longitude.value,
                locationName = _locationName.value,
                reporterName = _reporterName.value,
                reporterRole = _reporterRole.value,
                imageUris = _imageUris.value,
                notes = _notes.value
            )

            _submittedReport.value = report
            _submittedTriageResult.value = liveTriage
            _showTriageDialog.value = true
            _isSubmitting.value = false
        }
    }

    fun dismissTriageDialog() {
        _showTriageDialog.value = false
    }

    fun resetForm() {
        _species.value = Species.CATTLE
        _earTagOrHerdId.value = ""
        _selectedAnimal.value = null
        _selectedSymptoms.value = emptySet()
        _affectedCount.value = 1
        _totalHerdCount.value = 1
        _mortalityCount.value = 0
        _symptomOnsetDays.value = 1
        _notes.value = ""
        _imageUris.value = emptyList()
        _submittedReport.value = null
        _submittedTriageResult.value = null
        _showTriageDialog.value = false
    }

    class Factory(private val application: EasyVetApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SymptomReportViewModel::class.java)) {
                return SymptomReportViewModel(
                    symptomReportRepository = application.symptomReportRepository,
                    animalRepository = application.animalRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
        }
    }
}
