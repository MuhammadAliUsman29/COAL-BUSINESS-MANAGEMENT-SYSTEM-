package com.example.myapplication55.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
