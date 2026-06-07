package com.example.ngepet.presentation

import com.example.ngepet.data.local.UserPreferencesRepository
import com.example.ngepet.domain.model.*
import com.example.ngepet.domain.repository.BudgetRepository
import com.example.ngepet.domain.repository.CategoryRepository
import com.example.ngepet.domain.repository.TransactionRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private lateinit var transactionRepo: TransactionRepository
    private lateinit var categoryRepo: CategoryRepository
    private lateinit var budgetRepo: BudgetRepository
    private lateinit var userPrefs: UserPreferencesRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepo = mockk(relaxed = true)
        categoryRepo = mockk(relaxed = true)
        budgetRepo = mockk(relaxed = true)
        userPrefs = mockk(relaxed = true)

        every { userPrefs.userNameFlow } returns flowOf("Budi")
        every { userPrefs.hasCompletedOnboardingFlow } returns flowOf(true)
        every { userPrefs.initialBalanceFlow } returns flowOf(1_000_000L)
        every { categoryRepo.getAllCategories() } returns flowOf(emptyList())
        every { transactionRepo.getAllTransactions() } returns flowOf(emptyList())
        every { budgetRepo.getBudgets(any(), any()) } returns flowOf(emptyList())

        viewModel = MainViewModel(transactionRepo, categoryRepo, budgetRepo, userPrefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `userName emits from prefs`() = runTest {
        assertEquals("Budi", viewModel.userName.value)
    }

    @Test
    fun `hasCompletedOnboarding emits true`() = runTest {
        assertTrue(viewModel.hasCompletedOnboarding.value)
    }

    @Test
    fun `addTransaction calls repository`() = runTest {
        coEvery { transactionRepo.insertTransaction(any()) } just Runs

        viewModel.addTransaction(50000, 1L, "Makan siang", System.currentTimeMillis(), true)

        coVerify(exactly = 1) { transactionRepo.insertTransaction(match {
            it.amount == 50000.0 && it.categoryId == "1" && it.note == "Makan siang"
        })}
    }

    @Test
    fun `addTransaction emits success snackbar`() = runTest {
        coEvery { transactionRepo.insertTransaction(any()) } just Runs

        viewModel.addTransaction(50000, 1L, "Makan siang", System.currentTimeMillis(), true)

        val event = viewModel.snackbarEvent.first()
        assertTrue(event is SnackbarEvent.Success)
        assertEquals("Transaksi tersimpan", (event as SnackbarEvent.Success).message)
    }

    @Test
    fun `deleteTransaction calls repository`() = runTest {
        coEvery { transactionRepo.deleteTransaction(any()) } just Runs

        viewModel.deleteTransaction("42")

        coVerify(exactly = 1) { transactionRepo.deleteTransaction("42") }
    }

    @Test
    fun `deleteTransaction emits success snackbar`() = runTest {
        coEvery { transactionRepo.deleteTransaction(any()) } just Runs

        viewModel.deleteTransaction("42")

        val event = viewModel.snackbarEvent.first()
        assertTrue(event is SnackbarEvent.Success)
        assertEquals("Transaksi dihapus", (event as SnackbarEvent.Success).message)
    }

    @Test
    fun `addBudget calls repository`() = runTest {
        coEvery { budgetRepo.upsertBudget(any()) } just Runs

        viewModel.addBudget(1L, 500000L)

        coVerify(exactly = 1) { budgetRepo.upsertBudget(match {
            it.categoryId == "1" && it.limit == 500000.0
        })}
    }

    @Test
    fun `addBudget emits success snackbar`() = runTest {
        coEvery { budgetRepo.upsertBudget(any()) } just Runs

        viewModel.addBudget(1L, 500000L)

        val event = viewModel.snackbarEvent.first()
        assertTrue(event is SnackbarEvent.Success)
        assertEquals("Budget tersimpan", (event as SnackbarEvent.Success).message)
    }

    @Test
    fun `currentBalance is a StateFlow`() = runTest {
        assertNotNull(viewModel.currentBalance)
    }

    @Test
    fun `monthlyIncome is zero when no transactions`() = runTest {
        advanceUntilIdle()
        assertEquals(0L, viewModel.monthlyIncome.value)
    }

    @Test
    fun `monthlyExpense is zero when no transactions`() = runTest {
        advanceUntilIdle()
        assertEquals(0L, viewModel.monthlyExpense.value)
    }

    @Test
    fun `currentTip is not blank`() = runTest {
        advanceUntilIdle()
        assertTrue(viewModel.currentTip.value.isNotBlank())
    }
}
