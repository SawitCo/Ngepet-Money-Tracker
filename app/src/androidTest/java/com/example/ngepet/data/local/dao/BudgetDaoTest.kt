package com.example.ngepet.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ngepet.data.local.NgepetDatabase
import com.example.ngepet.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BudgetDaoTest {

    private lateinit var db: NgepetDatabase
    private lateinit var dao: com.example.ngepet.data.local.dao.BudgetDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NgepetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.budgetDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `insert and query budget by month and year`() = runTest {
        val budget = BudgetEntity(id = 0, categoryId = 1, limit = 500000, month = 6, year = 2026)

        dao.insertBudget(budget)

        val result = dao.getBudgets(6, 2026).first()
        assertEquals(1, result.size)
        assertEquals(500000L, result[0].limit)
        assertEquals(1L, result[0].categoryId)
    }

    @Test
    fun `query excludes different month`() = runTest {
        dao.insertBudget(BudgetEntity(id = 0, categoryId = 1, limit = 500000, month = 6, year = 2026))
        dao.insertBudget(BudgetEntity(id = 0, categoryId = 2, limit = 300000, month = 7, year = 2026))

        val result = dao.getBudgets(6, 2026).first()
        assertEquals(1, result.size)
        assertEquals(1L, result[0].categoryId)
    }

    @Test
    fun `delete budget removes record`() = runTest {
        val id = dao.insertBudget(BudgetEntity(id = 0, categoryId = 1, limit = 500000, month = 6, year = 2026))

        dao.deleteBudget(id)

        val result = dao.getBudgets(6, 2026).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `insert multiple budgets for same month`() = runTest {
        dao.insertBudget(BudgetEntity(id = 0, categoryId = 1, limit = 500000, month = 6, year = 2026))
        dao.insertBudget(BudgetEntity(id = 0, categoryId = 2, limit = 300000, month = 6, year = 2026))
        dao.insertBudget(BudgetEntity(id = 0, categoryId = 3, limit = 200000, month = 6, year = 2026))

        val result = dao.getBudgets(6, 2026).first()
        assertEquals(3, result.size)
    }
}
