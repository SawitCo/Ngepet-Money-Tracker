package com.example.ngepet.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ngepet.data.local.NgepetDatabase
import com.example.ngepet.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TransactionDaoTest {

    private lateinit var db: NgepetDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NgepetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.transactionDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `insert then getAll returns inserted data`() = runTest {
        val tx = TransactionEntity(id = 0, amount = 50000, categoryId = 1, note = "Makan", dateMillis = 1000, isExpense = true)

        dao.insertTransaction(tx)

        val result = dao.getAllTransactions().first()
        assertEquals(1, result.size)
        assertEquals(50000L, result[0].amount)
        assertEquals("Makan", result[0].note)
    }

    @Test
    fun `delete removes record`() = runTest {
        val tx = TransactionEntity(id = 0, amount = 50000, categoryId = 1, note = "Makan", dateMillis = 1000, isExpense = true)
        val id = dao.insertTransaction(tx)

        dao.deleteTransactionById(id)

        val result = dao.getAllTransactions().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAll orders by dateMillis DESC`() = runTest {
        dao.insertTransaction(TransactionEntity(id = 0, amount = 10000, categoryId = 1, note = "Old", dateMillis = 100, isExpense = true))
        dao.insertTransaction(TransactionEntity(id = 0, amount = 20000, categoryId = 1, note = "New", dateMillis = 200, isExpense = true))

        val result = dao.getAllTransactions().first()
        assertEquals(2, result.size)
        assertEquals("New", result[0].note)
        assertEquals("Old", result[1].note)
    }

    @Test
    fun `insert multiple transactions`() = runTest {
        dao.insertTransaction(TransactionEntity(id = 0, amount = 10000, categoryId = 1, note = "A", dateMillis = 100, isExpense = true))
        dao.insertTransaction(TransactionEntity(id = 0, amount = 20000, categoryId = 2, note = "B", dateMillis = 200, isExpense = false))
        dao.insertTransaction(TransactionEntity(id = 0, amount = 30000, categoryId = 3, note = "C", dateMillis = 300, isExpense = true))

        val result = dao.getAllTransactions().first()
        assertEquals(3, result.size)
    }
}
