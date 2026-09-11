package com.example.easyvet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "advisories")
data class AdvisoryEntity(
    @PrimaryKey
    val id: String,
    val diseaseName: String,
    val category: String, // e.g. "Biosecurity", "Vaccination", "Quarantine Protocol", "Outbreak Control"
    val title: String,
    val summary: String,
    val detailedSteps: List<String>,
    val targetSpecies: List<String>,
    val urgencyLevel: String = "HIGH",
    val updatedAt: Long = System.currentTimeMillis()
)
