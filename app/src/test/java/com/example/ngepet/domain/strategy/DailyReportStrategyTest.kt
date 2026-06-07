package com.example.ngepet.domain.strategy

import com.example.ngepet.domain.model.InputType
import com.example.ngepet.domain.model.Transaction
import com.example.ngepet.domain.model.TransactionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DailyReportStrategyTest {

    private lateinit var strategy: DailyReportStrategy

    @Before
    fun setup() {
        strategy = DailyReportStrategy()
    }

    @Test
    fun `calculate - empty list returns zeros`() {
        val result = strategy.calculate(emptyList())
        assertEquals(0.0, result.totalIncome, 0.01)
        assertEquals(0.0, result.totalExpense, 0.01)
    }

    @Test
    fun `calculate - includes today transactions`() {
        val now = System.currentTimeMillis()
        val transactions = listOf(
            fakeTransaction(amount = 25000.0, type = TransactionType.EXPENSE, dateMillis = now),
            fakeTransaction(amount = 10000.0, type = TransactionType.INCOME, dateMillis = now)
        )
        val result = strategy.calculate(transactions)
        assertEquals(25000.0, result.totalExpense, 0.01)
        assertEquals(10000.0, result.totalIncome, 0.01)
    }

    @Test
    fun `calculate - excludes yesterday transactions`() {
        val now = System.currentTimeMillis()
        val transactions = listOf(
            fakeTransaction(amount = 25000.0, type = TransactionType.EXPENSE, dateMillis = now),
            fakeTransaction(amount = 99999.0, type = TransactionType.EXPENSE, dateMillis = now - 2 * 86400000L)
        )
        val result = strategy.calculate(transactions)
        assertEquals(25000.0, result.totalExpense, 0.01)
    }

    @Test
    fun `calculate - period label is Harian`() {
        val result = strategy.calculate(emptyList())
        assertEquals("Harian", result.period)
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
