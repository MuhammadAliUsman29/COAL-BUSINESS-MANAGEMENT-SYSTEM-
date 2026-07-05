package com.example.myapplication55.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coal_stocks")
data class CoalStock(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val origin: String,
    val moistureLevel: String, // Dropdown: ++, --, +-
    val quantity: Double, // In KG
    val qualityGrade: String,
    val totalCapacity: Double, // Max capacity for this specific stock/pile in KG
    val lastUpdated: Long = System.currentTimeMillis()
)
