package com.example.ngepet.data.repository

import com.example.ngepet.data.local.dao.BudgetDao
import com.example.ngepet.data.local.entity.BudgetEntity
import com.example.ngepet.domain.model.Budget
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BudgetRepositoryImplTest {

    private lateinit var dao: BudgetDao
    private lateinit var repo: BudgetRepositoryImpl

    @Before
    fun setup() {
        dao = mockk()
        repo = BudgetRepositoryImpl(dao)
    }

    @Test
    fun `upsertBudget calls dao with correct entity`() = runTest {
        val budget = Budget("0", "1", 500000.0, 6, 2026)
        coEvery { dao.insertBudget(any()) } returns 1L

        repo.upsertBudget(budget)

        coVerify(exactly = 1) { dao.insertBudget(match {
            it.categoryId == 1L && it.limit == 500000L && it.month == 6 && it.year == 2026
        })}
    }

    @Test
    fun `getBudgets returns mapped domain models`() = runTest {
        val entities = listOf(
            BudgetEntity(id = 1, categoryId = 1, limit = 600000, month = 6, year = 2026),
            BudgetEntity(id = 2, categoryId = 2, limit = 300000, month = 6, year = 2026)
        )
        every { dao.getBudgets(6, 2026) } returns flowOf(entities)

        val result = repo.getBudgets(6, 2026).first()

        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals(600000.0, result[0].limit, 0.01)
        assertEquals(6, result[0].month)
        assertEquals("2", result[1].id)
    }

    @Test
    fun `deleteBudget calls dao with parsed Long`() = runTest {
        coEvery { dao.deleteBudget(any()) } just Runs

        repo.deleteBudget("5")

        coVerify(exactly = 1) { dao.deleteBudget(5L) }
    }

    @Test
    fun `deleteBudget - invalid id does not call dao`() = runTest {
        repo.deleteBudget("abc")

        coVerify(exactly = 0) { dao.deleteBudget(any()) }
    }
}
