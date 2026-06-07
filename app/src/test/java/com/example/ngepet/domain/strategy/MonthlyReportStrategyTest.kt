package com.example.ngepet.domain.strategy

import com.example.ngepet.domain.model.InputType
import com.example.ngepet.domain.model.Transaction
import com.example.ngepet.domain.model.TransactionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MonthlyReportStrategyTest {

    private lateinit var strategy: MonthlyReportStrategy

    @Before
    fun setup() {
        strategy = MonthlyReportStrategy()
    }

    @Test
    fun `calculate - empty list returns zeros`() {
        val result = strategy.calculate(emptyList())
        assertEquals(0.0, result.totalIncome, 0.01)
        assertEquals(0.0, result.totalExpense, 0.01)
        assertTrue(result.categoryBreakdown.isEmpty())
    }

    @Test
    fun `calculate - aggregates expenses correctly`() {
        val transactions = listOf(
            fakeTransaction(amount = 50000.0, type = TransactionType.EXPENSE, categoryId = "1"),
            fakeTransaction(amount = 30000.0, type = TransactionType.EXPENSE, categoryId = "1"),
            fakeTransaction(amount = 20000.0, type = TransactionType.EXPENSE, categoryId = "2")
        )
        val result = strategy.calculate(transactions)
        assertEquals(100000.0, result.totalExpense, 0.01)
        assertEquals(0.0, result.totalIncome, 0.01)
    }

    @Test
    fun `calculate - aggregates income correctly`() {
        val transactions = listOf(
            fakeTransaction(amount = 5000000.0, type = TransactionType.INCOME, categoryId = "3"),
            fakeTransaction(amount = 1000000.0, type = TransactionType.INCOME, categoryId = "3")
        )
        val result = strategy.calculate(transactions)
        assertEquals(6000000.0, result.totalIncome, 0.01)
        assertEquals(0.0, result.totalExpense, 0.01)
    }

    @Test
    fun `calculate - breakdown percentage is correct`() {
        val transactions = listOf(
            fakeTransaction(amount = 60000.0, type = TransactionType.EXPENSE, categoryId = "1"),
            fakeTransaction(amount = 40000.0, type = TransactionType.EXPENSE, categoryId = "2")
        )
        val result = strategy.calculate(transactions)
        assertEquals(2, result.categoryBreakdown.size)
        val sorted = result.categoryBreakdown.sortedByDescending { it.percentage }
        assertEquals(0.6, sorted[0].percentage, 0.01)
        assertEquals(0.4, sorted[1].percentage, 0.01)
    }

    @Test
    fun `calculate - period label is Bulanan`() {
        val result = strategy.calculate(emptyList())
        assertEquals("Bulanan", result.period)
    }

    private fun fakeTransaction(
        amount: Double = 10000.0,
        type: TransactionType = TransactionType.EXPENSE,
        categoryId: String = "1"
    ) = Transaction(
        id = "0",
        type = type,
        inputType = InputType.MANUAL,
        amount = amount,
        categoryId = categoryId,
        note = null,
        dateMillis = System.currentTimeMillis()
    )
}
