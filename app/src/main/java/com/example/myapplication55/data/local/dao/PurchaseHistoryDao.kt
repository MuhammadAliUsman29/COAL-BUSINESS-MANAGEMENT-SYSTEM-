package com.example.myapplication55.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication55.data.local.entities.PurchaseHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseHistoryDao {
    @Query("SELECT * FROM purchase_history WHERE userId = :userId ORDER BY date_timestamp DESC")
    fun getAllPurchases(userId: Long): Flow<List<PurchaseHistory>>

    @Insert
    suspend fun insertPurchase(purchase: PurchaseHistory)

    @Delete
    suspend fun deletePurchase(purchase: PurchaseHistory)

    @Query("DELETE FROM purchase_history")
    suspend fun deleteAllPurchases()
}
