package com.example.easyvet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.easyvet.data.model.HealthStatus

@Entity(tableName = "animals")
data class AnimalEntity(
    @PrimaryKey
    val id: String,
    val tagNumber: String,
    val name: String,
    val species: String,
    val breed: String,
    val ageMonths: Int,
    val gender: String,
    val weightKg: Double,
    val isHerdGroup: Boolean = false,
    val herdSize: Int = 1,
    val ownerName: String,
    val ownerContact: String,
    val location: String,
    val healthStatus: HealthStatus = HealthStatus.HEALTHY,
    val vaccinationHistory: List<String> = emptyList(),
    val lastCheckupDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val photoUri: String? = null
)
