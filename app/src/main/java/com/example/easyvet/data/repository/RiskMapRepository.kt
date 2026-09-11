package com.example.easyvet.data.repository

import com.example.easyvet.data.local.SeedData
import com.example.easyvet.data.local.dao.OutbreakAlertDao
import com.example.easyvet.data.local.entity.OutbreakAlertEntity
import kotlinx.coroutines.flow.Flow

class RiskMapRepository(private val outbreakAlertDao: OutbreakAlertDao) {

    val allAlerts: Flow<List<OutbreakAlertEntity>> = outbreakAlertDao.getAllAlerts()

    fun getHighRiskAlerts(minRiskScore: Int = 70): Flow<List<OutbreakAlertEntity>> {
        return outbreakAlertDao.getHighRiskAlerts(minRiskScore)
    }

    suspend fun getAlertById(id: String): OutbreakAlertEntity? {
        return outbreakAlertDao.getAlertById(id)
    }

    suspend fun addAlert(alert: OutbreakAlertEntity) {
        outbreakAlertDao.insertAlert(alert)
    }

    suspend fun ensureSeedData() {
        if (outbreakAlertDao.getAlertCount() == 0) {
            outbreakAlertDao.insertAllAlerts(SeedData.initialOutbreakAlerts)
        }
    }
}
