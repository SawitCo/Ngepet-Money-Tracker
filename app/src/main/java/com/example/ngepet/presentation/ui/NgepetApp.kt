package com.example.ngepet.presentation.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
import kotlinx.coroutines.launch

sealed class NavRoute(val route: String, val tab: NgepetTab) {
    data object Home : NavRoute("home", NgepetTab.Home)
    data object History : NavRoute("history", NgepetTab.History)
    data object Report : NavRoute("report", NgepetTab.Report)
    data object Budget : NavRoute("budget", NgepetTab.Budget)
}

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
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination
    val selectedTab = NgepetTab.entries.firstOrNull { tab ->
        currentRoute?.hierarchy?.any { it.route == navRouteForTab(tab) } == true
    } ?: NgepetTab.Home

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
                    onTabSelected = { tab ->
                        navController.navigate(navRouteForTab(tab)) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddClick = { sheetMode = InputSheetMode.Manual; editTxn = null }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = NavRoute.Home.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { slideInHorizontally(tween(200)) { fullWidth ->
                    val from = routeOrder[initialState.destination.route] ?: 0
                    val to = routeOrder[targetState.destination.route] ?: 0
                    if (to > from) fullWidth else -fullWidth
                } },
                exitTransition = { slideOutHorizontally(tween(200)) { fullWidth ->
                    val from = routeOrder[initialState.destination.route] ?: 0
                    val to = routeOrder[targetState.destination.route] ?: 0
                    if (to > from) -fullWidth else fullWidth
                } },
                popEnterTransition = { slideInHorizontally(tween(200)) { -it } },
                popExitTransition = { slideOutHorizontally(tween(200)) { it } }
            ) {
                composable(NavRoute.Home.route) {
                    HomeScreen(
                        userName = userName,
                        transactions = transactions.take(5),
                        currentBalance = currentBalance,
                        monthlyIncome = monthlyIncome,
                        monthlyExpense = monthlyExpense,
                        currentTip = currentTip
                    )
                }
                composable(NavRoute.History.route) {
                    HistoryScreen(
                        transactions = transactions,
                        onEditTransaction = { txn -> editTxn = txn; sheetMode = InputSheetMode.Manual },
                        onDeleteTransaction = onDeleteTransaction
                    )
                }
                composable(NavRoute.Report.route) {
                    ReportScreen(transactions = transactions)
                }
                composable(NavRoute.Budget.route) {
                    BudgetScreen(
                        budgets = budgetList,
                        categories = categories,
                        onAddBudget = onAddBudget,
                        onUpdateBudget = onUpdateBudget,
                        onDeleteBudget = onDeleteBudget
                    )
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

private val routeOrder = mapOf("home" to 0, "history" to 1, "report" to 2, "budget" to 3)

private fun navRouteForTab(tab: NgepetTab): String = when (tab) {
    NgepetTab.Home -> NavRoute.Home.route
    NgepetTab.History -> NavRoute.History.route
    NgepetTab.Report -> NavRoute.Report.route
    NgepetTab.Budget -> NavRoute.Budget.route
}

@Preview(showBackground = true)
@Composable
private fun NgepetPreview() {
    NgepetTheme { NgepetApp() }
}
