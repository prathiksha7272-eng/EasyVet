package com.example.easyvet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.easyvet.data.local.entity.AdvisoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdvisoryDao {

    @Query("SELECT * FROM advisories ORDER BY updatedAt DESC")
    fun getAllAdvisories(): Flow<List<AdvisoryEntity>>

    @Query("SELECT * FROM advisories WHERE id = :id")
    suspend fun getAdvisoryById(id: String): AdvisoryEntity?

    @Query("SELECT * FROM advisories WHERE diseaseName LIKE '%' || :diseaseName || '%'")
    fun getAdvisoriesForDisease(diseaseName: String): Flow<List<AdvisoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvisory(advisory: AdvisoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAdvisories(advisories: List<AdvisoryEntity>)

    @Query("SELECT COUNT(*) FROM advisories")
    suspend fun getAdvisoryCount(): Int
}
