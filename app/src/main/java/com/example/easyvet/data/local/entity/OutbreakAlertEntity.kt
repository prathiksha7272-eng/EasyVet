package com.example.easyvet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.easyvet.data.model.AlertSeverity

@Entity(tableName = "outbreak_alerts")
data class OutbreakAlertEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val diseaseName: String,
    val affectedSpecies: String,
    val severity: AlertSeverity,
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Double,
    val regionName: String,
    val activeCases: Int,
    val mortalityCount: Int,
    val riskScore: Int, // 0 - 100
    val advisory: String,
    val reportedDate: Long = System.currentTimeMillis(),
    val isConfirmed: Boolean = true
)
