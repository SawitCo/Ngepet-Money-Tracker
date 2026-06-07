package com.example.ngepet.domain.strategy

import com.example.ngepet.domain.model.InputType
import com.example.ngepet.domain.model.Transaction
import com.example.ngepet.domain.model.TransactionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WeeklyReportStrategyTest {

    private lateinit var strategy: WeeklyReportStrategy

    @Before
    fun setup() {
        strategy = WeeklyReportStrategy()
    }

    @Test
    fun `calculate - empty list returns zeros`() {
        val result = strategy.calculate(emptyList())
        assertEquals(0.0, result.totalIncome, 0.01)
        assertEquals(0.0, result.totalExpense, 0.01)
    }

    @Test
    fun `calculate - includes transactions from this week`() {
        val now = System.currentTimeMillis()
        val transactions = listOf(
            fakeTransaction(amount = 50000.0, type = TransactionType.EXPENSE, dateMillis = now),
            fakeTransaction(amount = 30000.0, type = TransactionType.EXPENSE, dateMillis = now - 3 * 86400000L)
        )
        val result = strategy.calculate(transactions)
        assertEquals(80000.0, result.totalExpense, 0.01)
    }

    @Test
    fun `calculate - excludes transactions older than 7 days`() {
        val now = System.currentTimeMillis()
        val transactions = listOf(
            fakeTransaction(amount = 50000.0, type = TransactionType.EXPENSE, dateMillis = now),
            fakeTransaction(amount = 99999.0, type = TransactionType.EXPENSE, dateMillis = now - 10 * 86400000L)
        )
        val result = strategy.calculate(transactions)
        assertEquals(50000.0, result.totalExpense, 0.01)
    }

    @Test
    fun `calculate - period label is Mingguan`() {
        val result = strategy.calculate(emptyList())
        assertEquals("Mingguan", result.period)
    }

    private fun fakeTransaction(
        amount: Double = 10000.0,
        type: TransactionType = TransactionType.EXPENSE,
        dateMillis: Long = System.currentTimeMillis()
    ) = Transaction(
        id = "0",
        type = type,
        inputType = InputType.MANUAL,
        amount = amount,
        categoryId = "1",
        note = null,
        dateMillis = dateMillis
    )
}
