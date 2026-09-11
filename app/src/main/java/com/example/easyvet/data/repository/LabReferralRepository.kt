package com.example.easyvet.data.repository

import com.example.easyvet.data.local.SeedData
import com.example.easyvet.data.local.dao.LabReferralDao
import com.example.easyvet.data.local.entity.LabReferralEntity
import com.example.easyvet.data.model.ReferralStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class LabReferralRepository(private val labReferralDao: LabReferralDao) {

    val allReferrals: Flow<List<LabReferralEntity>> = labReferralDao.getAllReferrals()

    suspend fun getReferralById(id: String): LabReferralEntity? {
        return labReferralDao.getReferralById(id)
    }

    suspend fun getReferralByTrackingNumber(trackingNumber: String): LabReferralEntity? {
        return labReferralDao.getReferralByTrackingNumber(trackingNumber)
    }

    suspend fun createReferral(
        symptomReportId: String?,
        animalTag: String,
        species: String,
        sampleType: String,
        suspectedDisease: String,
        urgency: String = "HIGH",
        destinationLab: String = "Central Veterinary Reference Laboratory",
        collectorName: String = "Community Field Officer",
        resultNotes: String? = null
    ): LabReferralEntity {
        val referralId = "LAB-${UUID.randomUUID().toString().take(8).uppercase()}"
        val trackingNo = "EV-LAB-${(100000..999999).random()}"
        val qrData = "EASYVET:LAB:$trackingNo:$animalTag:$suspectedDisease"

        val entity = LabReferralEntity(
            id = referralId,
            symptomReportId = symptomReportId,
            animalTag = animalTag,
            species = species,
            sampleType = sampleType,
            suspectedDisease = suspectedDisease,
            urgency = urgency,
            status = ReferralStatus.PENDING,
            destinationLab = destinationLab,
            collectorName = collectorName,
            collectionDate = System.currentTimeMillis(),
            testResults = null,
            resultNotes = resultNotes,
            trackingNumber = trackingNo,
            qrCodeData = qrData
        )

        labReferralDao.insertReferral(entity)
        return entity
    }

    suspend fun updateReferralStatus(id: String, status: ReferralStatus, results: String? = null, notes: String? = null) {
        labReferralDao.updateReferralStatusAndResult(id, status, results, notes)
    }

    suspend fun ensureSeedData() {
        if (labReferralDao.getReferralCount() == 0) {
            labReferralDao.insertAllReferrals(SeedData.initialLabReferrals)
        }
    }
}
