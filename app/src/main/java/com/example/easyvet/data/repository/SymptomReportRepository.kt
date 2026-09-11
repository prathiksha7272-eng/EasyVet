package com.example.easyvet.data.repository

import com.example.easyvet.data.local.SeedData
import com.example.easyvet.data.local.dao.SymptomReportDao
import com.example.easyvet.data.local.entity.SymptomReportEntity
import com.example.easyvet.data.model.Species
import com.example.easyvet.data.model.Symptom
import com.example.easyvet.data.model.SyncStatus
import com.example.easyvet.data.triage.TriageEngine
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SymptomReportRepository(private val symptomReportDao: SymptomReportDao) {

    val allReports: Flow<List<SymptomReportEntity>> = symptomReportDao.getAllReports()

    fun getReportsBySyncStatus(syncStatus: SyncStatus): Flow<List<SymptomReportEntity>> {
        return symptomReportDao.getReportsBySyncStatus(syncStatus)
    }

    suspend fun getReportById(id: String): SymptomReportEntity? {
        return symptomReportDao.getReportById(id)
    }

    suspend fun submitReport(
        animalId: String?,
        species: Species,
        affectedCount: Int,
        totalHerdCount: Int,
        mortalityCount: Int,
        selectedSymptoms: List<Symptom>,
        onsetDays: Int,
        latitude: Double,
        longitude: Double,
        locationName: String,
        reporterName: String,
        reporterRole: String,
        imageUris: List<String>,
        notes: String
    ): SymptomReportEntity {
        val triageResult = TriageEngine.evaluate(
            symptoms = selectedSymptoms,
            species = species,
            affectedCount = affectedCount,
            mortalityCount = mortalityCount,
            totalHerdCount = totalHerdCount
        )

        val reportId = "REP-${UUID.randomUUID().toString().take(8).uppercase()}"

        val reportEntity = SymptomReportEntity(
            id = reportId,
            animalId = animalId,
            species = species.displayName,
            affectedCount = affectedCount,
            totalHerdCount = totalHerdCount,
            mortalityCount = mortalityCount,
            selectedSymptoms = selectedSymptoms.map { it.name },
            symptomOnsetDays = onsetDays,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            triageSeverity = triageResult.severity,
            contagionRisk = triageResult.contagionRisk,
            suspectedDiseases = triageResult.suspectedDiseases,
            recommendedActions = triageResult.recommendedActions,
            reportedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING,
            reporterName = reporterName,
            reporterRole = reporterRole,
            imageUris = imageUris,
            notes = notes
        )

        symptomReportDao.insertReport(reportEntity)
        return reportEntity
    }

    suspend fun saveReport(report: SymptomReportEntity) {
        symptomReportDao.insertReport(report)
    }

    suspend fun updateSyncStatus(id: String, syncStatus: SyncStatus) {
        symptomReportDao.updateSyncStatus(id, syncStatus)
    }

    suspend fun syncPendingReports(): Int {
        val pending = symptomReportDao.getPendingReportsList()
        var syncedCount = 0
        for (report in pending) {
            // Simulate network sync to central server
            symptomReportDao.updateSyncStatus(report.id, SyncStatus.SYNCED)
            syncedCount++
        }
        return syncedCount
    }

    suspend fun ensureSeedData() {
        if (symptomReportDao.getReportCount() == 0) {
            symptomReportDao.insertAllReports(SeedData.initialSymptomReports)
        }
    }
}
