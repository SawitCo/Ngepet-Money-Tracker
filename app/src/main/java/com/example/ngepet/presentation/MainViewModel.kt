package com.example.ngepet.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngepet.data.local.NgepetDatabase
import com.example.ngepet.data.local.UserPreferencesRepository
import com.example.ngepet.data.local.entity.CategoryEntity
import com.example.ngepet.data.local.entity.TransactionEntity
import com.example.ngepet.presentation.ui.model.CategoryUi
import com.example.ngepet.presentation.ui.model.TransactionUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NgepetDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val userPreferencesRepository = UserPreferencesRepository(application)

    val userName: StateFlow<String?> = userPreferencesRepository.userNameFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val hasCompletedOnboarding: StateFlow<Boolean> = userPreferencesRepository.hasCompletedOnboardingFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val initialBalance: StateFlow<Long> = userPreferencesRepository.initialBalanceFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val currentBalance: StateFlow<Long> = combine(
        transactionDao.getAllTransactions(),
        userPreferencesRepository.initialBalanceFlow
    ) { txs, balance ->
        val netAmount = txs.sumOf { tx ->
            if (tx.isExpense) -tx.amount else tx.amount
        }
        balance + netAmount
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val categories: StateFlow<List<CategoryEntity>> = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionUi>> = combine(
        transactionDao.getAllTransactions(),
        categoryDao.getAllCategories()
    ) { txs, cats ->
        txs.map { tx ->
            val category = cats.find { it.id == tx.categoryId }
            TransactionUi(
                id = tx.id.toString(),
                amount = tx.amount,
                categoryName = category?.name ?: "Unknown",
                categoryIcon = category?.iconName ?: "Help",
                note = tx.note,
                dateMillis = tx.dateMillis,
                isExpense = tx.isExpense
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val currentCategories = categoryDao.getAllCategories().first()
            if (currentCategories.isEmpty()) {
                val initialCategories = listOf(
                    CategoryEntity(name = "Makanan", iconName = "Restaurant", isExpense = true),
                    CategoryEntity(name = "Transport", iconName = "Commute", isExpense = true),
                    CategoryEntity(name = "Gaji", iconName = "Payments", isExpense = false),
                    CategoryEntity(name = "Belanja", iconName = "ShoppingCart", isExpense = true),
                    CategoryEntity(name = "Hiburan", iconName = "Movie", isExpense = true),
                    CategoryEntity(name = "Tagihan", iconName = "Receipt", isExpense = true),
                    CategoryEntity(name = "Kesehatan", iconName = "LocalHospital", isExpense = true),
                    CategoryEntity(name = "Lainnya", iconName = "MoreHoriz", isExpense = true)
                )
                initialCategories.forEach { categoryDao.insertCategory(it) }
            }
        }
    }

    fun saveUserName(name: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveUserName(name)
        }
    }

    fun saveInitialBalance(balance: Long) {
        viewModelScope.launch {
            userPreferencesRepository.saveInitialBalance(balance)
        }
    }

    fun addTransaction(amount: Long, categoryId: Long, note: String, dateMillis: Long, isExpense: Boolean) {
        viewModelScope.launch {
            transactionDao.insertTransaction(
                TransactionEntity(
                    amount = amount,
                    categoryId = categoryId,
                    note = note,
                    dateMillis = dateMillis,
                    isExpense = isExpense
                )
            )
        }
    }
}
