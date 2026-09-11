package com.example.easyvet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.easyvet.data.local.entity.OutbreakAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutbreakAlertDao {

    @Query("SELECT * FROM outbreak_alerts ORDER BY reportedDate DESC")
    fun getAllAlerts(): Flow<List<OutbreakAlertEntity>>

    @Query("SELECT * FROM outbreak_alerts WHERE id = :id")
    suspend fun getAlertById(id: String): OutbreakAlertEntity?

    @Query("SELECT * FROM outbreak_alerts WHERE riskScore >= :minRiskScore ORDER BY riskScore DESC")
    fun getHighRiskAlerts(minRiskScore: Int = 70): Flow<List<OutbreakAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: OutbreakAlertEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAlerts(alerts: List<OutbreakAlertEntity>)

    @Update
    suspend fun updateAlert(alert: OutbreakAlertEntity)

    @Query("SELECT COUNT(*) FROM outbreak_alerts")
    suspend fun getAlertCount(): Int
}
