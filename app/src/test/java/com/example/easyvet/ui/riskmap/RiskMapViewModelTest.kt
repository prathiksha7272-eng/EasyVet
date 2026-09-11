package com.example.easyvet.ui.riskmap

import com.example.easyvet.data.local.dao.OutbreakAlertDao
import com.example.easyvet.data.local.entity.OutbreakAlertEntity
import com.example.easyvet.data.model.AlertSeverity
import com.example.easyvet.data.repository.RiskMapRepository
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RiskMapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

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

    private lateinit var viewModel: RiskMapViewModel
    private lateinit var alertDao: FakeAlertDao

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        alertDao = FakeAlertDao()
        val repository = RiskMapRepository(alertDao)
        viewModel = RiskMapViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun filteringBySpecies_filtersOutbreaksCorrectly() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        val alert1 = OutbreakAlertEntity(
            id = "ALT-1",
            title = "FMD Outbreak",
            diseaseName = "FMD",
            affectedSpecies = "Cattle, Buffalo",
            severity = AlertSeverity.CRITICAL,
            latitude = -6.7,
            longitude = 38.9,
            radiusKm = 20.0,
            regionName = "Coast",
            activeCases = 10,
            mortalityCount = 1,
            riskScore = 80,
            advisory = "Quarantine"
        )
        val alert2 = OutbreakAlertEntity(
            id = "ALT-2",
            title = "PPR Outbreak",
            diseaseName = "PPR",
            affectedSpecies = "Goat, Sheep",
            severity = AlertSeverity.HIGH,
            latitude = -6.8,
            longitude = 37.6,
            radiusKm = 15.0,
            regionName = "Morogoro",
            activeCases = 5,
            mortalityCount = 0,
            riskScore = 65,
            advisory = "Isolate goats"
        )

        alertDao.insertAllAlerts(listOf(alert1, alert2))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.filteredAlerts.size)

        viewModel.setSpeciesFilter("Goat")
        testDispatcher.scheduler.advanceUntilIdle()

        val filtered = viewModel.uiState.value.filteredAlerts
        assertEquals(1, filtered.size)
        assertEquals("ALT-2", filtered[0].id)
    }

    @Test
    fun selectingAlert_updatesStateAndTogglesViewMode() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        val alert = OutbreakAlertEntity(
            id = "ALT-1",
            title = "FMD Outbreak",
            diseaseName = "FMD",
            affectedSpecies = "Cattle",
            severity = AlertSeverity.CRITICAL,
            latitude = -6.7,
            longitude = 38.9,
            radiusKm = 20.0,
            regionName = "Coast",
            activeCases = 10,
            mortalityCount = 1,
            riskScore = 80,
            advisory = "Quarantine"
        )

        alertDao.insertAlert(alert)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectAlert(alert)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(alert, viewModel.uiState.value.selectedAlert)

        viewModel.toggleMapViewMode()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(MapViewMode.QUARANTINE_PERIMETERS, viewModel.uiState.value.mapViewMode)
    }
}
