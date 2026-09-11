package com.example.easyvet.ui.lab

import com.example.easyvet.data.local.dao.LabReferralDao
import com.example.easyvet.data.local.entity.LabReferralEntity
import com.example.easyvet.data.model.ReferralStatus
import com.example.easyvet.data.repository.LabReferralRepository
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
class LabReferralViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

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
            referrals.value = referrals.value.map {
                if (it.id == id) it.copy(status = status, testResults = results, resultNotes = notes) else it
            }
        }
        override suspend fun getReferralCount(): Int = referrals.value.size
    }

    private lateinit var viewModel: LabReferralViewModel
    private lateinit var labDao: FakeLabDao

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        labDao = FakeLabDao()
        val repository = LabReferralRepository(labDao)
        viewModel = LabReferralViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createNewReferral_insertsReferralAndUpdatesState() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.createNewReferral(
            animalTag = "TZ-TAG-101",
            species = "Cattle",
            sampleType = "Vesicular Fluid",
            suspectedDisease = "Foot and Mouth Disease",
            urgency = "URGENT",
            destinationLab = "National Vet Lab",
            collectorName = "Dr. Mwangi",
            notes = "Cold chain intact"
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.referrals.size)
        assertEquals("TZ-TAG-101", state.referrals[0].animalTag)
        assertNotNull(state.selectedReferral)
    }

    @Test
    fun updateReferralStatus_updatesStatusAndResultInState() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        val referral = LabReferralEntity(
            id = "LAB-1",
            animalTag = "TZ-TAG-102",
            species = "Goat",
            sampleType = "Nasal Swab",
            suspectedDisease = "PPR",
            urgency = "HIGH",
            status = ReferralStatus.PENDING,
            destinationLab = "Zonal Diagnostic Lab",
            collectorName = "Grace Temba",
            collectionDate = System.currentTimeMillis(),
            testResults = null,
            resultNotes = null,
            trackingNumber = "EV-LAB-12345",
            qrCodeData = "EASYVET:LAB:EV-LAB-12345"
        )

        labDao.insertReferral(referral)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateReferralStatusAndResult(
            referralId = "LAB-1",
            newStatus = ReferralStatus.RESULT_READY,
            resultOutcome = "POSITIVE - PPR Serotype A",
            resultNotes = "PCR test confirmed"
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val updated = labDao.getReferralById("LAB-1")
        assertEquals(ReferralStatus.RESULT_READY, updated?.status)
        assertEquals("POSITIVE - PPR Serotype A", updated?.testResults)
    }
}
