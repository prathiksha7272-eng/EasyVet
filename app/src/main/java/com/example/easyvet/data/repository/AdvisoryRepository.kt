package com.example.easyvet.data.repository

import com.example.easyvet.data.local.SeedData
import com.example.easyvet.data.local.dao.AdvisoryDao
import com.example.easyvet.data.local.entity.AdvisoryEntity
import kotlinx.coroutines.flow.Flow

class AdvisoryRepository(private val advisoryDao: AdvisoryDao) {

    val allAdvisories: Flow<List<AdvisoryEntity>> = advisoryDao.getAllAdvisories()

    fun getAdvisoriesForDisease(diseaseName: String): Flow<List<AdvisoryEntity>> {
        return advisoryDao.getAdvisoriesForDisease(diseaseName)
    }

    suspend fun getAdvisoryById(id: String): AdvisoryEntity? {
        return advisoryDao.getAdvisoryById(id)
    }

    suspend fun addAdvisory(advisory: AdvisoryEntity) {
        advisoryDao.insertAdvisory(advisory)
    }

    suspend fun ensureSeedData() {
        if (advisoryDao.getAdvisoryCount() == 0) {
            advisoryDao.insertAllAdvisories(SeedData.initialAdvisories)
        }
    }
}
