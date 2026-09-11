package com.example.easyvet.ui.ledger

import com.example.easyvet.data.local.dao.AnimalDao
import com.example.easyvet.data.local.entity.AnimalEntity
import com.example.easyvet.data.model.HealthStatus
import com.example.easyvet.data.model.Species
import com.example.easyvet.data.repository.AnimalRepository
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
class AnimalLedgerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAnimalDao : AnimalDao {
        val animals = MutableStateFlow<List<AnimalEntity>>(emptyList())
        override fun getAllAnimals(): Flow<List<AnimalEntity>> = animals
        override suspend fun getAnimalById(id: String): AnimalEntity? = animals.value.find { it.id == id }
        override suspend fun getAnimalByTag(tagNumber: String): AnimalEntity? = animals.value.find { it.tagNumber == tagNumber }
        override fun getAnimalsBySpecies(species: String): Flow<List<AnimalEntity>> = MutableStateFlow(animals.value.filter { it.species == species })
        override fun getAnimalsByHealthStatus(status: String): Flow<List<AnimalEntity>> = MutableStateFlow(animals.value.filter { it.healthStatus.name == status })
        override suspend fun insertAnimal(animal: AnimalEntity) { animals.value = animals.value + animal }
        override suspend fun insertAllAnimals(animals: List<AnimalEntity>) { this.animals.value = this.animals.value + animals }
        override suspend fun updateAnimal(animal: AnimalEntity) {
            animals.value = animals.value.map { if (it.id == animal.id) animal else it }
        }
        override suspend fun deleteAnimal(animal: AnimalEntity) {
            animals.value = animals.value.filter { it.id != animal.id }
        }
        override suspend fun getAnimalCount(): Int = animals.value.size
    }

    private lateinit var viewModel: AnimalLedgerViewModel
    private lateinit var animalDao: FakeAnimalDao

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        animalDao = FakeAnimalDao()
        val repository = AnimalRepository(animalDao)
        viewModel = AnimalLedgerViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun registerNewAnimal_addsAnimalToDatabase() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.registerNewAnimal(
            tagNumber = "TAG-999",
            name = "Bessie Dairy",
            species = Species.CATTLE.displayName,
            breed = "Friesian",
            ageMonths = 30,
            gender = "Female",
            weightKg = 400.0,
            isHerdGroup = false,
            herdSize = 1,
            ownerName = "John Doe",
            ownerContact = "123456",
            location = "Kibaha",
            healthStatus = HealthStatus.HEALTHY,
            initialVaccines = listOf("FMD Vaccine"),
            notes = "Healthy cow"
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.animals.size)
        assertEquals("TAG-999", state.animals[0].tagNumber)
    }

    @Test
    fun updateAnimalHealthStatus_updatesStatusInState() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        val animal = AnimalEntity(
            id = "ANM-1",
            tagNumber = "TAG-100",
            name = "Goat Flock",
            species = Species.GOAT.displayName,
            breed = "Local",
            ageMonths = 12,
            gender = "Female",
            weightKg = 30.0,
            isHerdGroup = true,
            herdSize = 15,
            ownerName = "Mama Juma",
            ownerContact = "987654",
            location = "Morogoro",
            healthStatus = HealthStatus.HEALTHY
        )

        animalDao.insertAnimal(animal)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateAnimalHealthStatus(animal, HealthStatus.QUARANTINED)
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = animalDao.getAnimalById("ANM-1")
        assertEquals(HealthStatus.QUARANTINED, updated?.healthStatus)
    }
}
