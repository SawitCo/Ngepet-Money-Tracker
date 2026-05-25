package com.example.ngepet.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngepet.data.local.NgepetDatabase
import com.example.ngepet.data.local.UserPreferencesRepository
import com.example.ngepet.data.repository.CategoryRepositoryImpl
import com.example.ngepet.data.repository.TransactionRepositoryImpl
import com.example.ngepet.domain.model.InputType
import com.example.ngepet.domain.model.Transaction
import com.example.ngepet.domain.model.TransactionType
import com.example.ngepet.domain.repository.CategoryRepository
import com.example.ngepet.domain.repository.TransactionRepository
import com.example.ngepet.presentation.ui.model.BudgetUi
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
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NgepetDatabase.getDatabase(application)
    private val transactionRepository: TransactionRepository =
        TransactionRepositoryImpl(database.transactionDao())
    private val categoryRepository: CategoryRepository =
        CategoryRepositoryImpl(database.categoryDao())
    private val userPreferencesRepository = UserPreferencesRepository(application)

    private val now = Calendar.getInstance()
    private val currentMonthStart: Long
    private val currentMonthEnd: Long

    private var voiceHelper: VoiceRecognitionHelper? = null
    private val _voiceResult = kotlinx.coroutines.flow.MutableStateFlow<VoiceResult?>(null)
    val voiceResult: kotlinx.coroutines.flow.StateFlow<VoiceResult?> = _voiceResult
    private val _isListening = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isListening: kotlinx.coroutines.flow.StateFlow<Boolean> = _isListening
    private val _voiceError = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val voiceError: kotlinx.coroutines.flow.StateFlow<String?> = _voiceError

    init {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        currentMonthStart = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        currentMonthEnd = cal.timeInMillis
    }

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

    val monthlyIncome: StateFlow<Long> = transactionRepository.getAllTransactions().map { txs ->
        txs.filter { it.type == TransactionType.INCOME && it.dateMillis in currentMonthStart until currentMonthEnd }
            .sumOf { it.amount.toLong() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthlyExpense: StateFlow<Long> = transactionRepository.getAllTransactions().map { txs ->
        txs.filter { it.type == TransactionType.EXPENSE && it.dateMillis in currentMonthStart until currentMonthEnd }
            .sumOf { it.amount.toLong() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val reportBreakdown: StateFlow<List<com.example.ngepet.domain.model.CategoryBreakdown>> = combine(
        transactionRepository.getAllTransactions(),
        categoryRepository.getAllCategories()
    ) { txs, cats ->
        val monthlyExpenses = txs.filter { it.type == TransactionType.EXPENSE && it.dateMillis in currentMonthStart until currentMonthEnd }
        val grandTotal = monthlyExpenses.sumOf { it.amount }
        if (grandTotal == 0.0) return@combine emptyList()
        monthlyExpenses.groupBy { it.categoryId }.map { (catId, txList) ->
            val cat = cats.find { it.id == catId }
            val amount = txList.sumOf { it.amount }
            com.example.ngepet.domain.model.CategoryBreakdown(
                categoryId = catId,
                categoryName = cat?.name ?: "Unknown",
                percentage = amount / grandTotal,
                amount = amount
            )
        }.sortedByDescending { it.percentage }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetList: StateFlow<List<BudgetUi>> = combine(
        transactionRepository.getAllTransactions(),
        categoryRepository.getAllCategories()
    ) { txs, cats ->
        val monthlyExpenses = txs.filter { it.type == TransactionType.EXPENSE && it.dateMillis in currentMonthStart until currentMonthEnd }
        val spentByCategory = monthlyExpenses.groupBy { it.categoryId }
            .mapValues { (_, txList) -> txList.sumOf { it.amount.toLong() } }

        val defaultLimits = mapOf(
            "Makanan" to 600_000L, "Belanja" to 300_000L,
            "Transport" to 500_000L, "Tagihan" to 200_000L,
            "Hiburan" to 200_000L, "Lainnya" to 200_000L
        )

        cats.filter { cat ->
            cat.name in defaultLimits || (spentByCategory.containsKey(cat.id) && spentByCategory[cat.id]!! > 0)
        }.mapNotNull { cat ->
            val spent = spentByCategory[cat.id] ?: 0L
            val limit = defaultLimits[cat.name] ?: maxOf(spent, 100_000L)
            val progress = if (limit > 0) spent.toFloat() / limit else 0f
            val status = when {
                progress >= 1f -> "Melebihi limit"
                progress >= 0.7f -> "Hampir habis"
                else -> "Aman"
            }
            val overAmount = if (progress >= 1f) spent - limit else 0L
            BudgetUi(
                category = cat.name,
                period = "Bulanan",
                used = "Rp ${formatAmount(spent)}",
                limit = "Rp ${formatAmount(limit)}",
                progress = progress,
                status = status,
                iconName = cat.iconName,
                spentAmount = spent,
                limitAmount = limit,
                overAmount = overAmount
            )
        }.sortedByDescending { it.progress }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentTip: StateFlow<String> = combine(
        monthlyIncome, monthlyExpense, reportBreakdown, transactions
    ) { income, expense, breakdown, txs ->
        generateTip(income, expense, breakdown, txs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), getDefaultTip(now))

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

    private fun generateTip(
        income: Long, expense: Long,
        breakdown: List<com.example.ngepet.domain.model.CategoryBreakdown>,
        txs: List<TransactionUi>
    ): String {
        val monthName = arrayOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )[now.get(Calendar.MONTH)]

        if (txs.isEmpty()) {
            return "Belum ada transaksi di bulan $monthName. Mulai catat pengeluaran pertama kamu!"
        }

        if (expense > income && income > 0) {
            val ratio = (expense - income) * 100 / income
            return "Pengeluaranmu $ratio% lebih besar dari pemasukan bulan ini. Coba kurangi belanja yang tidak perlu!"
        }

        if (income > 0) {
            val savingsRate = (income - expense) * 100 / income
            if (savingsRate >= 30) {
                return "Kebiasaan menabung yang baik! $savingsRate% pendapatanmu berhasil disisihkan bulan ini."
            }
            if (expense * 100 / income >= 70) {
                return "Pengeluaranmu sudah mencapai ${expense * 100 / income}% dari pemasukan. Perhatikan sisa bulan $monthName!"
            }
        }

        val topCategory = breakdown.maxByOrNull { it.amount }
        if (topCategory != null && topCategory.percentage > 0.4) {
            return "Pengeluaran terbesar bulan ini di kategori ${topCategory.categoryName} (${(topCategory.percentage * 100).toInt()}%). Coba evaluasi kembali!"
        }

        return getDefaultTip(now)
    }

    private fun getDefaultTip(cal: Calendar): String {
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        return when {
            dayOfMonth <= 7 -> "Awal bulan, saatnya buat anggaran bulan ${months[cal.get(Calendar.MONTH)]}!"
            dayOfMonth <= 20 -> "Coba alokasikan 20% pendapatanmu untuk tabungan darurat sebelum belanja."
            else -> "Akhir bulan, periksa kembali pengeluaran sebelum bulan baru tiba!"
        }
    }

    private fun formatAmount(amount: Long): String {
        return String.format("%,d", amount).replace(",", ".")
    }

    fun startVoiceRecognition() {
        if (voiceHelper == null) {
            voiceHelper = VoiceRecognitionHelper(getApplication())
            voiceHelper?.onResult = { result ->
                _voiceResult.value = result
            }
            voiceHelper?.onError = { error ->
                _voiceError.value = error
            }
            voiceHelper?.onListeningChanged = { listening ->
                _isListening.value = listening
            }
        }
        _voiceError.value = null
        voiceHelper?.startListening()
    }

    fun stopVoiceRecognition() {
        voiceHelper?.stopListening()
        _isListening.value = false
    }

    fun clearVoiceResult() {
        _voiceResult.value = null
        _voiceError.value = null
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
                    inputType = InputType.MANUAL,
                    amount = amount.toDouble(),
                    categoryId = categoryId.toString(),
                    note = note.ifBlank { null },
                    dateMillis = dateMillis
                )
            )
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
        }
    }
}
