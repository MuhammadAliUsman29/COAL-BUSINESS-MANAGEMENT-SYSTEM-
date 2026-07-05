package com.example.myapplication55.data.local.dao

import androidx.room.*
import com.example.myapplication55.data.local.entities.CoalStock
import kotlinx.coroutines.flow.Flow

@Dao
interface CoalStockDao {
    @Query("SELECT * FROM coal_stocks WHERE userId = :userId")
    fun getAllStocks(userId: Long): Flow<List<CoalStock>>

    @Query("SELECT * FROM coal_stocks WHERE id = :id")
    suspend fun getStockById(id: Long): CoalStock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: CoalStock)

    @Update
    suspend fun updateStock(stock: CoalStock)

    @Delete
    suspend fun deleteStock(stock: CoalStock)

    @Query("SELECT SUM(quantity) FROM coal_stocks WHERE userId = :userId")
    fun getTotalQuantity(userId: Long): Flow<Double?>

    @Query("DELETE FROM coal_stocks")
    suspend fun deleteAllStocks()
}
