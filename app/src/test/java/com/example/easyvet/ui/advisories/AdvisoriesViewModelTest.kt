package com.example.easyvet.ui.advisories

import com.example.easyvet.data.local.dao.AdvisoryDao
import com.example.easyvet.data.local.entity.AdvisoryEntity
import com.example.easyvet.data.repository.AdvisoryRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdvisoriesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAdvisoryDao : AdvisoryDao {
        val advisories = MutableStateFlow<List<AdvisoryEntity>>(emptyList())
        override fun getAllAdvisories(): Flow<List<AdvisoryEntity>> = advisories
        override suspend fun getAdvisoryById(id: String): AdvisoryEntity? = advisories.value.find { it.id == id }
        override fun getAdvisoriesForDisease(diseaseName: String): Flow<List<AdvisoryEntity>> =
            MutableStateFlow(advisories.value.filter { it.diseaseName.contains(diseaseName, ignoreCase = true) })
        override suspend fun insertAdvisory(advisory: AdvisoryEntity) { advisories.value = advisories.value + advisory }
        override suspend fun insertAllAdvisories(advisories: List<AdvisoryEntity>) { this.advisories.value = this.advisories.value + advisories }
        override suspend fun getAdvisoryCount(): Int = advisories.value.size
    }

    private lateinit var viewModel: AdvisoriesViewModel
    private lateinit var advisoryDao: FakeAdvisoryDao

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        advisoryDao = FakeAdvisoryDao()
        val repository = AdvisoryRepository(advisoryDao)
        viewModel = AdvisoriesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun languageSwitching_updatesTranslatedAdvisories() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        val advisory = AdvisoryEntity(
            id = "ADV-001",
            diseaseName = "Foot and Mouth Disease (FMD)",
            category = "Outbreak Control",
            title = "FMD Protocol",
            summary = "Immediate containment procedures.",
            detailedSteps = listOf("1. Isolate infected livestock."),
            targetSpecies = listOf("Cattle"),
            urgencyLevel = "CRITICAL"
        )

        advisoryDao.insertAdvisory(advisory)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AdvisoryLanguage.ENGLISH, viewModel.uiState.value.selectedLanguage)

        viewModel.setLanguage(AdvisoryLanguage.HINDI)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AdvisoryLanguage.HINDI, state.selectedLanguage)
        assertEquals(1, state.translatedAdvisories.size)
        assertTrue(state.translatedAdvisories[0].title.contains("एफएमडी") || state.translatedAdvisories[0].title.contains("FMD"))
    }

    @Test
    fun audioPlaybackToggle_startsAndPausesAudio() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        val advisory = AdvisoryEntity(
            id = "ADV-001",
            diseaseName = "Foot and Mouth Disease (FMD)",
            category = "Outbreak Control",
            title = "FMD Protocol",
            summary = "Immediate containment procedures.",
            detailedSteps = listOf("1. Isolate infected livestock."),
            targetSpecies = listOf("Cattle"),
            urgencyLevel = "CRITICAL"
        )

        advisoryDao.insertAdvisory(advisory)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleAudioPlayback("ADV-001")
        testDispatcher.scheduler.advanceTimeBy(100)

        assertEquals("ADV-001", viewModel.uiState.value.playingAdvisory?.id)
        assertTrue(viewModel.uiState.value.isPlayingAudio)

        viewModel.toggleAudioPlayback("ADV-001")
        testDispatcher.scheduler.advanceTimeBy(100)

        assertEquals(false, viewModel.uiState.value.isPlayingAudio)
    }
}
