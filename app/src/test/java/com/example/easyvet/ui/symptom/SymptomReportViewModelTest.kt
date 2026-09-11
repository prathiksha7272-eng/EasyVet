package com.example.easyvet.ui.symptom

import com.example.easyvet.data.local.dao.AnimalDao
import com.example.easyvet.data.local.dao.SymptomReportDao
import com.example.easyvet.data.local.entity.AnimalEntity
import com.example.easyvet.data.local.entity.SymptomReportEntity
import com.example.easyvet.data.model.Species
import com.example.easyvet.data.model.Symptom
import com.example.easyvet.data.model.SyncStatus
import com.example.easyvet.data.model.TriageSeverity
import com.example.easyvet.data.repository.AnimalRepository
import com.example.easyvet.data.repository.SymptomReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SymptomReportViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeSymptomReportDao : SymptomReportDao {
        private val reports = MutableStateFlow<List<SymptomReportEntity>>(emptyList())

        override fun getAllReports(): Flow<List<SymptomReportEntity>> = reports

        override suspend fun getReportById(id: String): SymptomReportEntity? =
            reports.value.find { it.id == id }

        override fun getReportsBySyncStatus(syncStatus: SyncStatus): Flow<List<SymptomReportEntity>> =
            MutableStateFlow(reports.value.filter { it.syncStatus == syncStatus })

        override suspend fun getPendingReportsList(): List<SymptomReportEntity> =
            reports.value.filter { it.syncStatus == SyncStatus.PENDING }

        override suspend fun insertReport(report: SymptomReportEntity) {
            reports.value = reports.value + report
        }

        override suspend fun insertAllReports(reports: List<SymptomReportEntity>) {
            this.reports.value = this.reports.value + reports
        }

        override suspend fun updateReport(report: SymptomReportEntity) {}

        override suspend fun updateSyncStatus(id: String, status: SyncStatus) {
            reports.value = reports.value.map {
                if (it.id == id) it.copy(syncStatus = status) else it
            }
        }

        override suspend fun getReportCount(): Int = reports.value.size
    }

    private class FakeAnimalDao : AnimalDao {
        private val animals = MutableStateFlow<List<AnimalEntity>>(emptyList())

        override fun getAllAnimals(): Flow<List<AnimalEntity>> = animals

        override suspend fun getAnimalById(id: String): AnimalEntity? = animals.value.find { it.id == id }

        override suspend fun getAnimalByTag(tagNumber: String): AnimalEntity? = animals.value.find { it.tagNumber == tagNumber }

        override fun getAnimalsBySpecies(species: String): Flow<List<AnimalEntity>> =
            MutableStateFlow(animals.value.filter { it.species == species })

        override fun getAnimalsByHealthStatus(status: String): Flow<List<AnimalEntity>> =
            MutableStateFlow(animals.value.filter { it.healthStatus.name == status })

        override suspend fun insertAnimal(animal: AnimalEntity) {
            animals.value = animals.value + animal
        }

        override suspend fun insertAllAnimals(animals: List<AnimalEntity>) {
            this.animals.value = this.animals.value + animals
        }

        override suspend fun updateAnimal(animal: AnimalEntity) {}

        override suspend fun deleteAnimal(animal: AnimalEntity) {}

        override suspend fun getAnimalCount(): Int = animals.value.size
    }

    private lateinit var viewModel: SymptomReportViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val reportDao = FakeSymptomReportDao()
        val animalDao = FakeAnimalDao()
        val symptomRepository = SymptomReportRepository(reportDao)
        val animalRepository = AnimalRepository(animalDao)
        viewModel = SymptomReportViewModel(symptomRepository, animalRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun toggleSymptom_updatesLiveTriageResultToEmergencyForFMD() = runTest {
        backgroundScope.launch { viewModel.formState.collect {} }

        viewModel.updateSpecies(Species.CATTLE)
        viewModel.toggleSymptom(Symptom.MOUTH_BLISTERS)
        viewModel.toggleSymptom(Symptom.FOOT_LESIONS)
        viewModel.toggleSymptom(Symptom.PROFUSE_SALIVATION)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.formState.value
        assertEquals(3, state.selectedSymptoms.size)
        assertEquals(TriageSeverity.EMERGENCY, state.liveTriageResult.severity)
        assertTrue(state.liveTriageResult.suspectedDiseases.any { it.contains("Foot and Mouth Disease") })
    }

    @Test
    fun mortalityCount_increasesAffectedCountAutomatically() = runTest {
        backgroundScope.launch { viewModel.formState.collect {} }

        viewModel.updateAffectedCount(2)
        viewModel.updateMortalityCount(5)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.formState.value
        assertEquals(5, state.mortalityCount)
        assertEquals(5, state.affectedCount)
    }

    @Test
    fun submitReport_createsReportAndOpensTriageDialog() = runTest {
        backgroundScope.launch { viewModel.formState.collect {} }

        viewModel.updateSpecies(Species.CATTLE)
        viewModel.toggleSymptom(Symptom.SUDDEN_MORTALITY)
        viewModel.toggleSymptom(Symptom.BLOODY_DIARRHEA)

        viewModel.submitReport()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.formState.value
        assertTrue(state.showTriageDialog)
        assertNotNull(state.submittedReport)
        assertNotNull(state.submittedTriageResult)
        assertEquals(TriageSeverity.EMERGENCY, state.submittedTriageResult?.severity)
    }
}
