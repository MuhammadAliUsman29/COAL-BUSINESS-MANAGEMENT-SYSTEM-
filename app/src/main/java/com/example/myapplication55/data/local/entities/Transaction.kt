package com.example.myapplication55.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    CASH_IN, CASH_OUT, STOCK_IN, STOCK_OUT
}

enum class PaymentStatus {
    PAID, PENDING
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val customerId: Long? = null,
    val stockId: Long? = null,
    val amount: Double = 0.0,
    val pricePerUnit: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val quantity: Double = 0.0,
    val type: TransactionType,
    val balanceAfter: Double = 0.0,
    val note: String = "",
    val imagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isPendingSale: Boolean = false,
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val receiptImagePath: String? = null
)
