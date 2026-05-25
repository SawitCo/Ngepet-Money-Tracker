package com.example.ngepet.di

import android.content.Context
import com.example.ngepet.data.local.NgepetDatabase
import com.example.ngepet.data.local.UserPreferencesRepository
import com.example.ngepet.data.local.dao.BudgetDao
import com.example.ngepet.data.local.dao.CategoryDao
import com.example.ngepet.data.local.dao.TransactionDao
import com.example.ngepet.data.repository.BudgetRepositoryImpl
import com.example.ngepet.data.repository.CategoryRepositoryImpl
import com.example.ngepet.data.repository.TransactionRepositoryImpl
import com.example.ngepet.domain.repository.BudgetRepository
import com.example.ngepet.domain.repository.CategoryRepository
import com.example.ngepet.domain.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NgepetDatabase {
        return NgepetDatabase.getDatabase(context)
    }

    @Provides
    fun provideTransactionDao(database: NgepetDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideCategoryDao(database: NgepetDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideBudgetDao(database: NgepetDatabase): BudgetDao {
        return database.budgetDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(transactionDao: TransactionDao): TransactionRepository {
        return TransactionRepositoryImpl(transactionDao)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(categoryDao: CategoryDao): CategoryRepository {
        return CategoryRepositoryImpl(categoryDao)
    }

    @Provides
    @Singleton
    fun provideBudgetRepository(budgetDao: BudgetDao): BudgetRepository {
        return BudgetRepositoryImpl(budgetDao)
    }
}
