package com.example.easyvet.ui.dashboard

import com.example.easyvet.data.local.dao.AnimalDao
import com.example.easyvet.data.local.dao.LabReferralDao
import com.example.easyvet.data.local.dao.OutbreakAlertDao
import com.example.easyvet.data.local.dao.SymptomReportDao
import com.example.easyvet.data.local.entity.AnimalEntity
import com.example.easyvet.data.local.entity.LabReferralEntity
import com.example.easyvet.data.local.entity.OutbreakAlertEntity
import com.example.easyvet.data.local.entity.SymptomReportEntity
import com.example.easyvet.data.model.ReferralStatus
import com.example.easyvet.data.model.SyncStatus
import com.example.easyvet.data.model.TriageSeverity
import com.example.easyvet.data.repository.AnimalRepository
import com.example.easyvet.data.repository.LabReferralRepository
import com.example.easyvet.data.repository.RiskMapRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeSymptomDao : SymptomReportDao {
        val reports = MutableStateFlow<List<SymptomReportEntity>>(emptyList())
        override fun getAllReports(): Flow<List<SymptomReportEntity>> = reports
        override suspend fun getReportById(id: String): SymptomReportEntity? = reports.value.find { it.id == id }
        override fun getReportsBySyncStatus(syncStatus: SyncStatus): Flow<List<SymptomReportEntity>> =
            MutableStateFlow(reports.value.filter { it.syncStatus == syncStatus })
        override suspend fun getPendingReportsList(): List<SymptomReportEntity> =
            reports.value.filter { it.syncStatus == SyncStatus.PENDING }
        override suspend fun insertReport(report: SymptomReportEntity) { reports.value = reports.value + report }
        override suspend fun insertAllReports(reports: List<SymptomReportEntity>) { this.reports.value = this.reports.value + reports }
        override suspend fun updateReport(report: SymptomReportEntity) {}
        override suspend fun updateSyncStatus(id: String, status: SyncStatus) {
            reports.value = reports.value.map { if (it.id == id) it.copy(syncStatus = status) else it }
        }
        override suspend fun getReportCount(): Int = reports.value.size
    }

    private class FakeLabDao : LabReferralDao {
        val referrals = MutableStateFlow<List<LabReferralEntity>>(emptyList())
        override fun getAllReferrals(): Flow<List<LabReferralEntity>> = referrals
        override suspend fun getReferralById(id: String): LabReferralEntity? = referrals.value.find { it.id == id }
        override suspend fun getReferralByTrackingNumber(trackingNumber: String): LabReferralEntity? =
            referrals.value.find { it.trackingNumber == trackingNumber }
        override suspend fun insertReferral(referral: LabReferralEntity) { referrals.value = referrals.value + referral }
        override suspend fun insertAllReferrals(referrals: List<LabReferralEntity>) { this.referrals.value = this.referrals.value + referrals }
        override suspend fun updateReferral(referral: LabReferralEntity) {}
        override suspend fun updateReferralStatusAndResult(id: String, status: ReferralStatus, results: String?, notes: String?) {
            referrals.value = referrals.value.map { if (it.id == id) it.copy(status = status) else it }
        }
        override suspend fun getReferralCount(): Int = referrals.value.size
    }

    private class FakeAnimalDao : AnimalDao {
        val animals = MutableStateFlow<List<AnimalEntity>>(emptyList())
        override fun getAllAnimals(): Flow<List<AnimalEntity>> = animals
        override suspend fun getAnimalById(id: String): AnimalEntity? = animals.value.find { it.id == id }
        override suspend fun getAnimalByTag(tagNumber: String): AnimalEntity? = animals.value.find { it.tagNumber == tagNumber }
        override fun getAnimalsBySpecies(species: String): Flow<List<AnimalEntity>> = MutableStateFlow(animals.value.filter { it.species == species })
        override fun getAnimalsByHealthStatus(status: String): Flow<List<AnimalEntity>> = MutableStateFlow(animals.value.filter { it.healthStatus.name == status })
        override suspend fun insertAnimal(animal: AnimalEntity) { animals.value = animals.value + animal }
        override suspend fun insertAllAnimals(animals: List<AnimalEntity>) { this.animals.value = this.animals.value + animals }
        override suspend fun updateAnimal(animal: AnimalEntity) {}
        override suspend fun deleteAnimal(animal: AnimalEntity) {}
        override suspend fun getAnimalCount(): Int = animals.value.size
    }

    private class FakeAlertDao : OutbreakAlertDao {
        val alerts = MutableStateFlow<List<OutbreakAlertEntity>>(emptyList())
        override fun getAllAlerts(): Flow<List<OutbreakAlertEntity>> = alerts
        override suspend fun getAlertById(id: String): OutbreakAlertEntity? = alerts.value.find { it.id == id }
        override fun getHighRiskAlerts(minRiskScore: Int): Flow<List<OutbreakAlertEntity>> =
            MutableStateFlow(alerts.value.filter { it.riskScore >= minRiskScore })
        override suspend fun insertAlert(alert: OutbreakAlertEntity) { alerts.value = alerts.value + alert }
        override suspend fun insertAllAlerts(alerts: List<OutbreakAlertEntity>) { this.alerts.value = this.alerts.value + alerts }
        override suspend fun updateAlert(alert: OutbreakAlertEntity) {}
        override suspend fun getAlertCount(): Int = alerts.value.size
    }

    private lateinit var viewModel: DashboardViewModel
    private lateinit var symptomDao: FakeSymptomDao

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        symptomDao = FakeSymptomDao()
        val labDao = FakeLabDao()
        val animalDao = FakeAnimalDao()
        val alertDao = FakeAlertDao()

        val symptomRepo = SymptomReportRepository(symptomDao)
        val labRepo = LabReferralRepository(labDao)
        val animalRepo = AnimalRepository(animalDao)
        val riskRepo = RiskMapRepository(alertDao)

        viewModel = DashboardViewModel(symptomRepo, labRepo, animalRepo, riskRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun syncPendingReports_updatesPendingSyncCountAndShowsMessage() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        // Add 2 pending reports
        symptomDao.insertReport(
            SymptomReportEntity(
                id = "REP-001",
                species = "Cattle",
                triageSeverity = TriageSeverity.HIGH,
                contagionRisk = com.example.easyvet.data.model.ContagionRisk.HIGH,
                selectedSymptoms = listOf("HIGH_FEVER"),
                suspectedDiseases = listOf("LSD"),
                recommendedActions = listOf("Isolate"),
                syncStatus = SyncStatus.PENDING
            )
        )
        symptomDao.insertReport(
            SymptomReportEntity(
                id = "REP-002",
                species = "Goat",
                triageSeverity = TriageSeverity.MEDIUM,
                contagionRisk = com.example.easyvet.data.model.ContagionRisk.MODERATE,
                selectedSymptoms = listOf("LOSS_OF_APPETITE"),
                suspectedDiseases = listOf("General"),
                recommendedActions = listOf("Monitor"),
                syncStatus = SyncStatus.PENDING
            )
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.pendingOfflineSyncsCount)

        viewModel.syncPendingReports()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.pendingOfflineSyncsCount)
        assertFalse(state.isSyncing)
        assertNotNull(state.syncResultMessage)
    }
}
