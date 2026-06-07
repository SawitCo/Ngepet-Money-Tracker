package com.example.ngepet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngepet.data.local.UserPreferencesRepository
import com.example.ngepet.domain.model.Budget
import com.example.ngepet.domain.model.CategoryBreakdown
import com.example.ngepet.domain.model.InputType
import com.example.ngepet.domain.model.Transaction
import com.example.ngepet.domain.model.TransactionType
import com.example.ngepet.domain.repository.BudgetRepository
import com.example.ngepet.domain.repository.CategoryRepository
import com.example.ngepet.domain.repository.TransactionRepository
import com.example.ngepet.presentation.ui.model.BudgetUi
import com.example.ngepet.presentation.ui.model.CategoryUi
import com.example.ngepet.presentation.ui.model.TransactionUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

sealed class SnackbarEvent {
    data class Success(val message: String) : SnackbarEvent()
    data class Error(val message: String) : SnackbarEvent()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val now = Calendar.getInstance()
    private val currentMonthStart: Long
    private val currentMonthEnd: Long

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

    private val _snackbarEvent = Channel<SnackbarEvent>(Channel.BUFFERED)
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

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
                categoryId = tx.categoryId,
                note = tx.note ?: "",
                dateMillis = tx.dateMillis,
                isExpense = tx.type == TransactionType.EXPENSE,
                source = if (tx.inputType == InputType.VOICE) "Suara" else "Manual"
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

    val reportBreakdown: StateFlow<List<CategoryBreakdown>> = combine(
        transactionRepository.getAllTransactions(),
        categoryRepository.getAllCategories()
    ) { txs, cats ->
        val monthlyExpenses = txs.filter { it.type == TransactionType.EXPENSE && it.dateMillis in currentMonthStart until currentMonthEnd }
        val grandTotal = monthlyExpenses.sumOf { it.amount }
        if (grandTotal == 0.0) return@combine emptyList()
        monthlyExpenses.groupBy { it.categoryId }.map { (catId, txList) ->
            val cat = cats.find { it.id == catId }
            val amount = txList.sumOf { it.amount }
            CategoryBreakdown(
                categoryId = catId,
                categoryName = cat?.name ?: "Unknown",
                percentage = amount / grandTotal,
                amount = amount
            )
        }.sortedByDescending { it.percentage }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetList: StateFlow<List<BudgetUi>> = combine(
        budgetRepository.getBudgets(now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR)),
        transactionRepository.getAllTransactions(),
        categoryRepository.getAllCategories()
    ) { budgets, txs, cats ->
        val monthlyExpenses = txs.filter { it.type == TransactionType.EXPENSE && it.dateMillis in currentMonthStart until currentMonthEnd }
        val spentByCategory = monthlyExpenses.groupBy { it.categoryId }
            .mapValues { (_, txList) -> txList.sumOf { it.amount.toLong() } }

        budgets.mapNotNull { budget ->
            val cat = cats.find { it.id == budget.categoryId } ?: return@mapNotNull null
            val spent = spentByCategory[cat.id] ?: 0L
            val limit = budget.limit.toLong()
            val progress = if (limit > 0) spent.toFloat() / limit else 0f
            val status = when {
                progress >= 1f -> "Melebihi limit"
                progress >= 0.7f -> "Hampir habis"
                else -> "Aman"
            }
            val overAmount = if (progress >= 1f) spent - limit else 0L
            BudgetUi(
                id = budget.id,
                categoryId = cat.id,
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
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val dailyTips = listOf(
        "Coba alokasikan 20% pendapatanmu untuk tabungan darurat sebelum belanja.",
        "Catat setiap pengeluaran kecil — kopi, parkir, jajan — mereka bisa bikin boros tanpa sadar.",
        "Aturan 50/30/20: 50% kebutuhan, 30% keinginan, 20% tabungan. Coba terapkan bulan ini!",
        "Prioritaskan kebutuhan daripada keinginan.",
        "Menabung jadi lebih mudah kalau dipotong langsung dari pemasukan di awal bulan.",
        "Kurangi jajan di luar — masak di rumah bisa hemat sampai 40% pengeluaran makan.",
        "Coba pantau pengeluaran transportasi kamu — mungkin ada rute yang lebih hemat.",
        "Belanja bulanan dengan daftar belanja bisa cegah impulsive buying.",
        "Bayar tagihan lebih awal biar tidak kena denda dan lebih tenang.",
        "Kesehatan adalah investasi. Sisihkan dana untuk olahraga atau cek kesehatan rutin.",
        "Evaluasi langganan bulanan — mungkin ada yang jarang dipakai tapi tetap bayar.",
        "Sisihkan THR atau bonus sebagai tabungan, bukan untuk belanja impulsif.",
        "Hiburan itu penting, tapi tetapkan batas anggaran hiburan bulanan.",
        "Utang konsumtif adalah musuh tabungan. Prioritaskan lunasi cicilan.",
        "Liburan bisa hemat dengan rencana: booking awal, cari promo, dan pisahkan budget liburan.",
        "Review pengeluaran mingguan setiap hari Minggu untuk evaluasi.",
        "Gunakan cash atau debit — kartu kredit bikin pengeluaran terasa lebih abstrak.",
        "Awal bulan, saatnya buat anggaran bulan ini!",
        "Akhir bulan, periksa kembali pengeluaran sebelum bulan baru tiba!",
        "Kebiasaan finansial baik dimulai dari langkah kecil yang konsisten."
    )

    private fun getDefaultTip(cal: Calendar): String {
        val index = cal.get(Calendar.DAY_OF_YEAR) % dailyTips.size
        return dailyTips[index]
    }

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
                    "Pekerjaan" to "Payments",
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
        breakdown: List<CategoryBreakdown>,
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

    private fun formatAmount(amount: Long): String {
        return String.format("%,d", amount).replace(",", ".")
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
            try {
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
                _snackbarEvent.send(SnackbarEvent.Success("Transaksi tersimpan"))
            } catch (e: Exception) {
                _snackbarEvent.send(SnackbarEvent.Error("Gagal menyimpan. Coba lagi."))
            }
        }
    }

    fun updateTransaction(id: String, amount: Long, categoryId: Long, note: String, dateMillis: Long, isExpense: Boolean) {
        viewModelScope.launch {
            try {
                transactionRepository.updateTransaction(
                    Transaction(
                        id = id,
                        type = if (isExpense) TransactionType.EXPENSE else TransactionType.INCOME,
                        inputType = InputType.MANUAL,
                        amount = amount.toDouble(),
                        categoryId = categoryId.toString(),
                        note = note.ifBlank { null },
                        dateMillis = dateMillis
                    )
                )
                _snackbarEvent.send(SnackbarEvent.Success("Transaksi diperbarui"))
            } catch (e: Exception) {
                _snackbarEvent.send(SnackbarEvent.Error("Gagal memperbarui. Coba lagi."))
            }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(id)
                _snackbarEvent.send(SnackbarEvent.Success("Transaksi dihapus"))
            } catch (e: Exception) {
                _snackbarEvent.send(SnackbarEvent.Error("Gagal menghapus. Coba lagi."))
            }
        }
    }

    fun addBudget(categoryId: Long, limit: Long) {
        viewModelScope.launch {
            try {
                budgetRepository.upsertBudget(
                    Budget(
                        id = "0",
                        categoryId = categoryId.toString(),
                        limit = limit.toDouble(),
                        month = now.get(Calendar.MONTH) + 1,
                        year = now.get(Calendar.YEAR)
                    )
                )
                _snackbarEvent.send(SnackbarEvent.Success("Budget tersimpan"))
            } catch (e: Exception) {
                _snackbarEvent.send(SnackbarEvent.Error("Gagal menyimpan budget."))
            }
        }
    }

    fun updateBudget(id: String, limit: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val budgets = budgetRepository.getBudgets(
                    now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR)
                ).first()
                val existing = budgets.find { it.id == id } ?: return@launch
                budgetRepository.upsertBudget(
                    existing.copy(limit = limit.toDouble())
                )
                _snackbarEvent.send(SnackbarEvent.Success("Budget diperbarui"))
            } catch (e: Exception) {
                _snackbarEvent.send(SnackbarEvent.Error("Gagal memperbarui budget."))
            }
        }
    }

    fun deleteBudget(id: String) {
        viewModelScope.launch {
            try {
                budgetRepository.deleteBudget(id)
                _snackbarEvent.send(SnackbarEvent.Success("Budget dihapus"))
            } catch (e: Exception) {
                _snackbarEvent.send(SnackbarEvent.Error("Gagal menghapus budget."))
            }
        }
    }
}
