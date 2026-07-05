package com.example.myapplication55.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val name: String,
    val phoneNumber: String,
    val totalBalance: Double = 0.0, // Cumulative balance (Credit/Debit)
    val pendingBalance: Double = 0.0, // Amount owed by customer
    val lastTransactionDate: Long = System.currentTimeMillis()
)
