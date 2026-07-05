package com.example.myapplication55.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication55.data.local.dao.ActivityLogDao
import com.example.myapplication55.data.local.dao.CoalStockDao
import com.example.myapplication55.data.local.dao.CustomerDao
import com.example.myapplication55.data.local.dao.PurchaseHistoryDao
import com.example.myapplication55.data.local.dao.TransactionDao
import com.example.myapplication55.data.local.dao.UserDao
import com.example.myapplication55.data.local.entities.ActivityLog
import com.example.myapplication55.data.local.entities.CoalStock
import com.example.myapplication55.data.local.entities.Customer
import com.example.myapplication55.data.local.entities.PurchaseHistory
import com.example.myapplication55.data.local.entities.Transaction
import com.example.myapplication55.data.local.entities.User

@Database(
    entities = [CoalStock::class, Customer::class, Transaction::class, User::class, ActivityLog::class, PurchaseHistory::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coalStockDao(): CoalStockDao
    abstract fun customerDao(): CustomerDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun purchaseHistoryDao(): PurchaseHistoryDao

    companion object {
        const val DATABASE_NAME = "cms_database"
    }
}
