package com.example.easyvet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.easyvet.data.model.ReferralStatus

@Entity(tableName = "lab_referrals")
data class LabReferralEntity(
    @PrimaryKey
    val id: String,
    val symptomReportId: String? = null,
    val animalTag: String,
    val species: String,
    val sampleType: String, // e.g. "Vesicular Fluid", "Whole Blood", "Nasal Swab", "Tissue Specimen"
    val suspectedDisease: String,
    val urgency: String = "HIGH", // "URGENT", "HIGH", "NORMAL", "ROUTINE"
    val status: ReferralStatus = ReferralStatus.PENDING,
    val destinationLab: String = "Central Veterinary Diagnostic Laboratory",
    val collectorName: String = "Dr. J. Mwangi",
    val collectionDate: Long = System.currentTimeMillis(),
    val testResults: String? = null,
    val resultNotes: String? = null,
    val trackingNumber: String,
    val qrCodeData: String
)
