package com.example.easyvet.data.repository

import com.example.easyvet.data.local.dao.AnimalDao
import com.example.easyvet.data.local.entity.AnimalEntity
import com.example.easyvet.data.local.SeedData
import kotlinx.coroutines.flow.Flow

class AnimalRepository(private val animalDao: AnimalDao) {

    val allAnimals: Flow<List<AnimalEntity>> = animalDao.getAllAnimals()

    suspend fun getAnimalById(id: String): AnimalEntity? {
        return animalDao.getAnimalById(id)
    }

    suspend fun getAnimalByTag(tagNumber: String): AnimalEntity? {
        return animalDao.getAnimalByTag(tagNumber)
    }

    fun getAnimalsBySpecies(species: String): Flow<List<AnimalEntity>> {
        return animalDao.getAnimalsBySpecies(species)
    }

    fun getAnimalsByHealthStatus(status: String): Flow<List<AnimalEntity>> {
        return animalDao.getAnimalsByHealthStatus(status)
    }

    suspend fun addAnimal(animal: AnimalEntity) {
        animalDao.insertAnimal(animal)
    }

    suspend fun updateAnimal(animal: AnimalEntity) {
        animalDao.updateAnimal(animal)
    }

    suspend fun deleteAnimal(animal: AnimalEntity) {
        animalDao.deleteAnimal(animal)
    }

    suspend fun ensureSeedData() {
        if (animalDao.getAnimalCount() == 0) {
            animalDao.insertAllAnimals(SeedData.initialAnimals)
        }
    }
}
