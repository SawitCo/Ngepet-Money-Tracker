package com.example.ngepet.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngepet.data.local.UserPreferencesRepository
import com.example.ngepet.data.repository.CategoryRepositoryImpl
import com.example.ngepet.data.repository.TransactionRepositoryImpl
import com.example.ngepet.domain.model.Transaction
import com.example.ngepet.domain.model.TransactionType
import com.example.ngepet.data.local.NgepetDatabase
import com.example.ngepet.data.local.entity.CategoryEntity
import com.example.ngepet.domain.repository.CategoryRepository
import com.example.ngepet.domain.repository.TransactionRepository
import com.example.ngepet.presentation.ui.model.CategoryUi
import com.example.ngepet.presentation.ui.model.TransactionUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NgepetDatabase.getDatabase(application)
    private val transactionRepository: TransactionRepository =
        TransactionRepositoryImpl(database.transactionDao())
    private val categoryRepository: CategoryRepository =
        CategoryRepositoryImpl(database.categoryDao())
    private val userPreferencesRepository = UserPreferencesRepository(application)

    val userName: StateFlow<String?> = userPreferencesRepository.userNameFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val hasCompletedOnboarding: StateFlow<Boolean> = userPreferencesRepository.hasCompletedOnboardingFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val initialBalance: StateFlow<Long> = userPreferencesRepository.initialBalanceFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val categories: StateFlow<List<CategoryUi>> =
        categoryRepository.getAllCategories().map { cats ->
            cats.map { CategoryUi(id = it.id, name = it.name, iconName = it.iconName) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionUi>> = combine(
        transactionRepository.getAllTransactions(),
        categoryRepository.getAllCategories()
    ) { txs, cats ->
        txs.map { tx ->
            val category = cats.find { it.id == tx.categoryId }
            TransactionUi(
                id = tx.id,
                amount = tx.amount.toLong(),
                categoryName = category?.name ?: "Unknown",
                categoryIcon = category?.iconName ?: "Help",
                note = tx.note ?: "",
                dateMillis = tx.dateMillis,
                isExpense = tx.type == TransactionType.EXPENSE
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentBalance: StateFlow<Long> = combine(
        transactionRepository.getAllTransactions(),
        userPreferencesRepository.initialBalanceFlow
    ) { txs, balance ->
        val netAmount = txs.sumOf { tx ->
            if (tx.type == TransactionType.EXPENSE) -tx.amount.toLong() else tx.amount.toLong()
        }
        balance + netAmount
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val currentCategories = categoryRepository.getAllCategories().first()
            if (currentCategories.isEmpty()) {
                val initialCategories = listOf(
                    "Makanan" to "Restaurant",
                    "Transport" to "Commute",
                    "Gaji" to "Payments",
                    "Belanja" to "ShoppingCart",
                    "Hiburan" to "Movie",
                    "Tagihan" to "Receipt",
                    "Kesehatan" to "LocalHospital",
                    "Lainnya" to "MoreHoriz"
                )
                initialCategories.forEach { (name, icon) ->
                    categoryRepository.insertCategory(
                        com.example.ngepet.domain.model.Category(
                            id = "0", name = name, iconName = icon
                        )
                    )
                }
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

    fun addTransaction(
        amount: Long, categoryId: Long, note: String,
        dateMillis: Long, isExpense: Boolean
    ) {
        viewModelScope.launch {
            transactionRepository.insertTransaction(
                Transaction(
                    id = "0",
                    type = if (isExpense) TransactionType.EXPENSE else TransactionType.INCOME,
                    inputType = com.example.ngepet.domain.model.InputType.MANUAL,
                    amount = amount.toDouble(),
                    categoryId = categoryId.toString(),
                    note = note.ifBlank { null },
                    dateMillis = dateMillis
                )
            )
        }
    }
}
