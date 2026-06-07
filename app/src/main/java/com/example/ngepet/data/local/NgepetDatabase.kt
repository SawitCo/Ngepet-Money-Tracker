package com.example.ngepet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ngepet.data.local.dao.BudgetDao
import com.example.ngepet.data.local.dao.CategoryDao
import com.example.ngepet.data.local.dao.TransactionDao
import com.example.ngepet.data.local.entity.BudgetEntity
import com.example.ngepet.data.local.entity.CategoryEntity
import com.example.ngepet.data.local.entity.TransactionEntity

@Database(entities = [TransactionEntity::class, CategoryEntity::class, BudgetEntity::class], version = 2, exportSchema = false)
abstract class NgepetDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: NgepetDatabase? = null

        fun getDatabase(context: Context): NgepetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NgepetDatabase::class.java,
                    "ngepet_database"
                ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
