package com.example.myapplication55.data.local.dao

import androidx.room.*
import com.example.myapplication55.data.local.entities.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllTransactions(userId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsByCustomer(customerId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE stockId = :stockId ORDER BY timestamp DESC")
    fun getTransactionsByStock(stockId: Long): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM transactions WHERE customerId = :customerId")
    suspend fun deleteTransactionsByCustomer(customerId: Long)

    @Query("DELETE FROM transactions WHERE stockId = :stockId")
    suspend fun deleteTransactionsByStock(stockId: Long)

    @Query("SELECT * FROM transactions WHERE userId = :userId AND timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    suspend fun getTransactionsInRange(userId: Long, start: Long, end: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?
}
