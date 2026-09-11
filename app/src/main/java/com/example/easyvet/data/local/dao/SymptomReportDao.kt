package com.example.easyvet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.easyvet.data.local.entity.SymptomReportEntity
import com.example.easyvet.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomReportDao {

    @Query("SELECT * FROM symptom_reports ORDER BY reportedAt DESC")
    fun getAllReports(): Flow<List<SymptomReportEntity>>

    @Query("SELECT * FROM symptom_reports WHERE id = :id")
    suspend fun getReportById(id: String): SymptomReportEntity?

    @Query("SELECT * FROM symptom_reports WHERE syncStatus = :syncStatus")
    fun getReportsBySyncStatus(syncStatus: SyncStatus): Flow<List<SymptomReportEntity>>

    @Query("SELECT * FROM symptom_reports WHERE syncStatus = 'PENDING'")
    suspend fun getPendingReportsList(): List<SymptomReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: SymptomReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReports(reports: List<SymptomReportEntity>)

    @Update
    suspend fun updateReport(report: SymptomReportEntity)

    @Query("UPDATE symptom_reports SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("SELECT COUNT(*) FROM symptom_reports")
    suspend fun getReportCount(): Int
}
