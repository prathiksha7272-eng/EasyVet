package com.example.easyvet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.easyvet.data.model.ContagionRisk
import com.example.easyvet.data.model.SyncStatus
import com.example.easyvet.data.model.TriageSeverity

@Entity(tableName = "symptom_reports")
data class SymptomReportEntity(
    @PrimaryKey
    val id: String,
    val animalId: String? = null,
    val species: String,
    val affectedCount: Int = 1,
    val totalHerdCount: Int = 1,
    val mortalityCount: Int = 0,
    val selectedSymptoms: List<String>, // Symptom enum names or display names
    val symptomOnsetDays: Int = 1,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "",
    val triageSeverity: TriageSeverity,
    val contagionRisk: ContagionRisk,
    val suspectedDiseases: List<String>,
    val recommendedActions: List<String>,
    val reportedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val reporterName: String = "Field Paravet",
    val reporterRole: String = "Community Health Worker",
    val imageUris: List<String> = emptyList(),
    val notes: String = ""
)
