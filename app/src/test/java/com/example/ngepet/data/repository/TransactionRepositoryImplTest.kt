package com.example.ngepet.data.repository

import com.example.ngepet.data.local.dao.TransactionDao
import com.example.ngepet.data.local.entity.TransactionEntity
import com.example.ngepet.domain.model.InputType
import com.example.ngepet.domain.model.Transaction
import com.example.ngepet.domain.model.TransactionType
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TransactionRepositoryImplTest {

    private lateinit var dao: TransactionDao
    private lateinit var repo: TransactionRepositoryImpl

    @Before
    fun setup() {
        dao = mockk()
        repo = TransactionRepositoryImpl(dao)
    }

    @Test
    fun `insertTransaction calls dao with correct entity`() = runTest {
        val tx = Transaction("0", TransactionType.EXPENSE, InputType.MANUAL, 50000.0, "1", "Makan", 1000L)
        coEvery { dao.insertTransaction(any()) } returns 1L

        repo.insertTransaction(tx)

        coVerify(exactly = 1) { dao.insertTransaction(match {
            it.amount == 50000L && it.categoryId == 1L && it.note == "Makan" && it.isExpense
        })}
    }

    @Test
    fun `insertTransaction - income maps isExpense false`() = runTest {
        val tx = Transaction("0", TransactionType.INCOME, InputType.MANUAL, 1000000.0, "3", "Gaji", 2000L)
        coEvery { dao.insertTransaction(any()) } returns 1L

        repo.insertTransaction(tx)

        coVerify { dao.insertTransaction(match { !it.isExpense }) }
    }

    @Test
    fun `getAllTransactions maps entities to domain models`() = runTest {
        val entities = listOf(
            TransactionEntity(id = 2, amount = 1000000, categoryId = 3, note = "Gaji", dateMillis = 2000, isExpense = false),
            TransactionEntity(id = 1, amount = 50000, categoryId = 1, note = "Makan", dateMillis = 1000, isExpense = true)
        )
        every { dao.getAllTransactions() } returns flowOf(entities)

        val result = repo.getAllTransactions().first()

        assertEquals(2, result.size)
        // DAO sorts by dateMillis DESC, so Gaji (2000ms) comes first
        assertEquals("2", result[0].id)
        assertEquals(1000000.0, result[0].amount, 0.01)
        assertEquals(TransactionType.INCOME, result[0].type)
        assertEquals("Gaji", result[0].note)
        assertEquals("1", result[1].id)
        assertEquals(TransactionType.EXPENSE, result[1].type)
    }

    @Test
    fun `deleteTransaction calls dao with parsed Long`() = runTest {
        coEvery { dao.deleteTransactionById(any()) } just Runs

        repo.deleteTransaction("42")

        coVerify(exactly = 1) { dao.deleteTransactionById(42L) }
    }

    @Test
    fun `deleteTransaction - invalid id does not call dao`() = runTest {
        repo.deleteTransaction("not_a_number")

        coVerify(exactly = 0) { dao.deleteTransactionById(any()) }
    }
}
