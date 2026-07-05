package com.example.myapplication55

import android.app.Application
import androidx.room.Room
import com.example.myapplication55.data.local.AppDatabase
import com.example.myapplication55.data.repository.CmsRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CmsApplication : Application() {
    private val database by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val repository by lazy {
        CmsRepository(
            database.coalStockDao(),
            database.customerDao(),
            database.transactionDao(),
            database.userDao(),
            database.activityLogDao(),
            database.purchaseHistoryDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        MainScope().launch {
            repository.checkAndPrepopulate()
        }
    }
}
