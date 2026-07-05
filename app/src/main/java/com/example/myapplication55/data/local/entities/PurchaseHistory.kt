package com.example.myapplication55.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_history")
data class PurchaseHistory(
    @PrimaryKey(autoGenerate = true)
    val purchase_id: Long = 0,
    val userId: Long,
    val item_name: String,
    val quantity: Double,
    val unit_price: Double,
    val total_cost: Double,
    val date_timestamp: Long = System.currentTimeMillis(),
    val image_url: String? = null
)
