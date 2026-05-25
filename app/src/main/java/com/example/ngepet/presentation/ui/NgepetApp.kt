package com.example.ngepet.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ngepet.presentation.MainViewModel
import com.example.ngepet.presentation.SnackbarEvent
import com.example.ngepet.presentation.ui.model.CategoryUi
import com.example.ngepet.presentation.ui.model.TransactionUi
import com.example.ngepet.presentation.ui.screens.AddTransactionSheet
import com.example.ngepet.presentation.ui.screens.BudgetScreen
import com.example.ngepet.presentation.ui.screens.HistoryScreen
import com.example.ngepet.presentation.ui.screens.HomeScreen
import com.example.ngepet.presentation.ui.screens.OnboardingScreen
import com.example.ngepet.presentation.ui.screens.ReportScreen
import com.example.ngepet.presentation.ui.theme.NgepetTheme
import com.example.ngepet.presentation.ui.theme.SurfaceWarm
import com.example.ngepet.domain.model.CategoryBreakdown
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun NgepetApp(viewModel: MainViewModel = hiltViewModel()) {
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val categoriesEntity by viewModel.categories.collectAsState()
    val initialBalance by viewModel.initialBalance.collectAsState()
    val currentBalance by viewModel.currentBalance.collectAsState()
    val monthlyIncome by viewModel.monthlyIncome.collectAsState()
    val monthlyExpense by viewModel.monthlyExpense.collectAsState()
    val reportBreakdown by viewModel.reportBreakdown.collectAsState()
    val budgetList by viewModel.budgetList.collectAsState()
    val currentTip by viewModel.currentTip.collectAsState()

    val categories = categoriesEntity.map {
        CategoryUi(id = it.id.toString(), name = it.name, iconName = it.iconName)
    }

    if (!hasCompletedOnboarding) {
        OnboardingScreen(
            initialName = userName ?: "",
            initialBalance = initialBalance,
            onComplete = { name, balance ->
                viewModel.saveUserName(name.ifBlank { "Teman" })
                viewModel.saveInitialBalance(balance)
            }
        )
        return
    }

    MainAppContent(
        userName = userName ?: "Teman",
        transactions = transactions,
        categories = categories,
        currentBalance = currentBalance,
        monthlyIncome = monthlyIncome,
        monthlyExpense = monthlyExpense,
        reportBreakdown = reportBreakdown,
        budgetList = budgetList,
        currentTip = currentTip,
        onAddTransaction = { amount, categoryId, note, dateMillis, isExpense ->
            viewModel.addTransaction(amount, categoryId, note, dateMillis, isExpense)
        },
        onUpdateTransaction = { id, amount, categoryId, note, dateMillis, isExpense ->
            viewModel.updateTransaction(id, amount, categoryId, note, dateMillis, isExpense)
        },
        onDeleteTransaction = { id -> viewModel.deleteTransaction(id) },
        onAddBudget = { catId, limit -> viewModel.addBudget(catId, limit) },
        onUpdateBudget = { id, limit -> viewModel.updateBudget(id, limit) },
        onDeleteBudget = { id -> viewModel.deleteBudget(id) },
        snackbarEvent = viewModel.snackbarEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppContent(
    userName: String,
    transactions: List<TransactionUi>,
    categories: List<CategoryUi>,
    currentBalance: Long,
    monthlyIncome: Long,
    monthlyExpense: Long,
    reportBreakdown: List<CategoryBreakdown>,
    budgetList: List<com.example.ngepet.presentation.ui.model.BudgetUi>,
    currentTip: String,
    onAddTransaction: (Long, Long, String, Long, Boolean) -> Unit,
    onUpdateTransaction: (String, Long, Long, String, Long, Boolean) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onAddBudget: (Long, Long) -> Unit,
    onUpdateBudget: (String, Long) -> Unit,
    onDeleteBudget: (String) -> Unit,
    snackbarEvent: kotlinx.coroutines.flow.Flow<SnackbarEvent>
) {
    var selectedTab by remember { mutableStateOf(NgepetTab.Home) }
    var sheetMode by remember { mutableStateOf<InputSheetMode?>(null) }
    var editTxn by remember { mutableStateOf<TransactionUi?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(snackbarEvent) {
        snackbarEvent.collect { event ->
            val msg = when (event) {
                is SnackbarEvent.Success -> event.message
                is SnackbarEvent.Error -> event.message
            }
            scope.launch { snackbarHostState.showSnackbar(msg) }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = SurfaceWarm,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onAddClick = { sheetMode = InputSheetMode.Manual; editTxn = null }
                )
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                color = SurfaceWarm
            ) {
                AnimatedContent(targetState = selectedTab,
                    transitionSpec = {
                        val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                        slideInHorizontally(animationSpec = tween(200)) { width -> direction * width } togetherWith
                            slideOutHorizontally(animationSpec = tween(200)) { width -> -direction * width }
                    },
                    label = "TabSlide"
                ) { tab ->
                    when (tab) {
                        NgepetTab.Home -> HomeScreen(
                            userName = userName,
                            transactions = transactions.take(5),
                            currentBalance = currentBalance,
                            monthlyIncome = monthlyIncome,
                            monthlyExpense = monthlyExpense,
                            currentTip = currentTip
                        )
                        NgepetTab.History -> HistoryScreen(
                            transactions = transactions,
                            onEditTransaction = { txn ->
                                editTxn = txn
                                sheetMode = InputSheetMode.Manual
                            },
                            onDeleteTransaction = onDeleteTransaction
                        )
                        NgepetTab.Report -> ReportScreen(transactions = transactions)
                        NgepetTab.Budget -> BudgetScreen(
                            budgets = budgetList,
                            categories = categories,
                            onAddBudget = onAddBudget,
                            onUpdateBudget = onUpdateBudget,
                            onDeleteBudget = onDeleteBudget
                        )
                    }
                }
            }
        }

        val currentMode = sheetMode
        if (currentMode != null) {
            ModalBottomSheet(
                onDismissRequest = { sheetMode = null; editTxn = null },
                sheetState = sheetState
            ) {
                AddTransactionSheet(
                    mode = currentMode,
                    categories = categories,
                    editTxn = editTxn,
                    onModeChange = { sheetMode = it },
                    onClose = { sheetMode = null; editTxn = null },
                    onSave = { amount, categoryId, note, dateMillis, isExpense ->
                        onAddTransaction(amount, categoryId, note, dateMillis, isExpense)
                        sheetMode = null
                    },
                    onUpdate = { id, amount, categoryId, note, dateMillis, isExpense ->
                        onUpdateTransaction(id, amount, categoryId, note, dateMillis, isExpense)
                        sheetMode = null; editTxn = null
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NgepetPreview() {
    NgepetTheme {
        NgepetApp()
    }
}
