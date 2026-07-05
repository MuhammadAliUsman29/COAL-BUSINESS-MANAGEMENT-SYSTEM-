package com.example.myapplication55.data.local.dao

import androidx.room.*
import com.example.myapplication55.data.local.entities.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE userId = :userId ORDER BY lastTransactionDate DESC")
    fun getAllCustomers(userId: Long): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT SUM(totalBalance) FROM customers WHERE userId = :userId")
    fun getTotalCustomerBalance(userId: Long): Flow<Double?>
    
    @Query("SELECT COUNT(*) FROM customers WHERE userId = :userId")
    suspend fun getCustomerCount(userId: Long): Int

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()
}
