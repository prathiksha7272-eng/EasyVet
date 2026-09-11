package com.example.easyvet.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.easyvet.data.local.entity.AnimalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {

    @Query("SELECT * FROM animals ORDER BY lastCheckupDate DESC")
    fun getAllAnimals(): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animals WHERE id = :id")
    suspend fun getAnimalById(id: String): AnimalEntity?

    @Query("SELECT * FROM animals WHERE tagNumber = :tagNumber")
    suspend fun getAnimalByTag(tagNumber: String): AnimalEntity?

    @Query("SELECT * FROM animals WHERE species = :species")
    fun getAnimalsBySpecies(species: String): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animals WHERE healthStatus = :status")
    fun getAnimalsByHealthStatus(status: String): Flow<List<AnimalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: AnimalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAnimals(animals: List<AnimalEntity>)

    @Update
    suspend fun updateAnimal(animal: AnimalEntity)

    @Delete
    suspend fun deleteAnimal(animal: AnimalEntity)

    @Query("SELECT COUNT(*) FROM animals")
    suspend fun getAnimalCount(): Int
}
