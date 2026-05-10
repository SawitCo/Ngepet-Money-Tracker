package com.example.ngepet.domain.strategy

import com.example.ngepet.domain.model.CategoryBreakdown
import com.example.ngepet.domain.model.ReportData
import com.example.ngepet.domain.model.Transaction
import com.example.ngepet.domain.model.TransactionType

interface ReportStrategy {
    fun calculate(transactions: List<Transaction>): ReportData
}

class MonthlyReportStrategy : ReportStrategy {
    override fun calculate(transactions: List<Transaction>): ReportData {
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val income = transactions.filter { it.type == TransactionType.INCOME }
        val totalExpense = expenses.sumOf { it.amount }
        val totalIncome = income.sumOf { it.amount }
        val breakdown = expenses.groupBy { it.categoryId }.map { (catId, txs) ->
            val catTotal = txs.sumOf { it.amount }
            CategoryBreakdown(
                categoryName = catId,
                percentage = if (totalExpense > 0) catTotal / totalExpense * 100 else 0.0,
                amount = catTotal,
                colorHex = "#3B6D11"
            )
        }
        return ReportData(
            period = "Bulanan",
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            categoryBreakdown = breakdown
        )
    }
}

class DailyReportStrategy : ReportStrategy {
    override fun calculate(transactions: List<Transaction>): ReportData {
        val today = transactions.filter {
            val msInDay = 86400000L
            System.currentTimeMillis() - it.dateMillis < msInDay
        }
        val expenses = today.filter { it.type == TransactionType.EXPENSE }
        val income = today.filter { it.type == TransactionType.INCOME }
        return ReportData(
            period = "Harian",
            totalIncome = income.sumOf { it.amount },
            totalExpense = expenses.sumOf { it.amount },
            categoryBreakdown = emptyList()
        )
    }
}

class WeeklyReportStrategy : ReportStrategy {
    override fun calculate(transactions: List<Transaction>): ReportData {
        val weekAgo = System.currentTimeMillis() - 7 * 86400000L
        val thisWeek = transactions.filter { it.dateMillis >= weekAgo }
        val expenses = thisWeek.filter { it.type == TransactionType.EXPENSE }
        val income = thisWeek.filter { it.type == TransactionType.INCOME }
        return ReportData(
            period = "Mingguan",
            totalIncome = income.sumOf { it.amount },
            totalExpense = expenses.sumOf { it.amount },
            categoryBreakdown = emptyList()
        )
    }
}

class CategoryReportStrategy(
    private val categoryId: String
) : ReportStrategy {
    override fun calculate(transactions: List<Transaction>): ReportData {
        val filtered = transactions.filter { it.categoryId == categoryId }
        val expenses = filtered.filter { it.type == TransactionType.EXPENSE }
        val income = filtered.filter { it.type == TransactionType.INCOME }
        return ReportData(
            period = "Kategori",
            totalIncome = income.sumOf { it.amount },
            totalExpense = expenses.sumOf { it.amount },
            categoryBreakdown = emptyList()
        )
    }
}
