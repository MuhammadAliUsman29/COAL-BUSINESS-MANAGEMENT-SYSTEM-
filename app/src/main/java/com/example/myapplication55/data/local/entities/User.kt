package com.example.myapplication55.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val cnic: String,
    val mobileNumber: String,
    val profilePicPath: String? = null,
    val passwordHash: String,
    val balance: Double = 0.0,
    val isAdmin: Boolean = false
)
