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
            CategoryBreakdown(categoryId = catId, percentage = if (totalExpense > 0) catTotal / totalExpense else 0.0, amount = catTotal)
        }
        return ReportData("Bulanan", totalIncome, totalExpense, breakdown)
    }
}

class DailyReportStrategy : ReportStrategy {
    override fun calculate(transactions: List<Transaction>): ReportData {
        val today = transactions.filter { System.currentTimeMillis() - it.dateMillis < 86400000L }
        val expenses = today.filter { it.type == TransactionType.EXPENSE }
        val income = today.filter { it.type == TransactionType.INCOME }
        return ReportData("Harian", income.sumOf { it.amount }, expenses.sumOf { it.amount }, emptyList())
    }
}

class WeeklyReportStrategy : ReportStrategy {
    override fun calculate(transactions: List<Transaction>): ReportData {
        val thisWeek = transactions.filter { it.dateMillis >= System.currentTimeMillis() - 7 * 86400000L }
        val expenses = thisWeek.filter { it.type == TransactionType.EXPENSE }
        val income = thisWeek.filter { it.type == TransactionType.INCOME }
        return ReportData("Mingguan", income.sumOf { it.amount }, expenses.sumOf { it.amount }, emptyList())
    }
}
