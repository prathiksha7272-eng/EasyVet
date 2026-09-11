package com.example.easyvet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val phone: String = "",
    val role: String = "Veterinary Officer",
    val createdAt: Long = System.currentTimeMillis()
)
