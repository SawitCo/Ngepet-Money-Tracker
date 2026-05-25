package com.example.ngepet.presentation.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ngepet.presentation.MainViewModel
import com.example.ngepet.presentation.ui.model.CategoryUi
import com.example.ngepet.presentation.ui.screens.AddTransactionSheet
import com.example.ngepet.presentation.ui.screens.BudgetScreen
import com.example.ngepet.presentation.ui.screens.HistoryScreen
import com.example.ngepet.presentation.ui.screens.HomeScreen
import com.example.ngepet.presentation.ui.screens.OnboardingScreen
import com.example.ngepet.presentation.ui.screens.ReportScreen
import com.example.ngepet.presentation.ui.theme.NgepetTheme
import com.example.ngepet.presentation.ui.theme.SurfaceWarm
import java.util.Date
import com.example.ngepet.domain.model.CategoryBreakdown

@Composable
fun NgepetApp(viewModel: MainViewModel = viewModel()) {
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
    val voiceResult by viewModel.voiceResult.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val voiceError by viewModel.voiceError.collectAsState()

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
        voiceResult = voiceResult,
        isListening = isListening,
        voiceError = voiceError,
        onAddTransaction = { amount, categoryId, note, isExpense ->
            viewModel.addTransaction(amount, categoryId, note, Date().time, isExpense)
        },
        onStartVoice = { viewModel.startVoiceRecognition() },
        onStopVoice = { viewModel.stopVoiceRecognition() },
        onClearVoice = { viewModel.clearVoiceResult() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppContent(
    userName: String,
    transactions: List<com.example.ngepet.presentation.ui.model.TransactionUi>,
    categories: List<CategoryUi>,
    currentBalance: Long,
    monthlyIncome: Long,
    monthlyExpense: Long,
    reportBreakdown: List<com.example.ngepet.domain.model.CategoryBreakdown>,
    budgetList: List<com.example.ngepet.presentation.ui.model.BudgetUi>,
    currentTip: String,
    voiceResult: com.example.ngepet.presentation.VoiceResult?,
    isListening: Boolean,
    voiceError: String?,
    onAddTransaction: (Long, Long, String, Boolean) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onClearVoice: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(NgepetTab.Home) }
    var sheetMode by remember { mutableStateOf<InputSheetMode?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = SurfaceWarm,
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onAddClick = { sheetMode = InputSheetMode.Manual }
                )
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = SurfaceWarm
            ) {
                Crossfade(targetState = selectedTab, animationSpec = tween(150)) { tab ->
                    when (tab) {
                        NgepetTab.Home -> HomeScreen(
                            userName = userName,
                            transactions = transactions.take(5),
                            currentBalance = currentBalance,
                            monthlyIncome = monthlyIncome,
                            monthlyExpense = monthlyExpense,
                            currentTip = currentTip
                        )
                        NgepetTab.History -> HistoryScreen(transactions = transactions)
                        NgepetTab.Report -> ReportScreen(
                            totalExpense = monthlyExpense,
                            breakdown = reportBreakdown
                        )
                        NgepetTab.Budget -> BudgetScreen(budgets = budgetList)
                    }
                }
            }
        }

        sheetMode?.let { mode ->
            ModalBottomSheet(
                onDismissRequest = {
                    sheetMode = null
                    onStopVoice()
                    onClearVoice()
                },
                sheetState = sheetState
            ) {
                AddTransactionSheet(
                    mode = mode,
                    categories = categories,
                    voiceResult = voiceResult,
                    isListening = isListening,
                    voiceError = voiceError,
                    onModeChange = { sheetMode = it },
                    onClose = { sheetMode = null },
                    onSave = { amount, categoryId, note, isExpense ->
                        onAddTransaction(amount, categoryId, note, isExpense)
                        sheetMode = null
                    },
                    onStartVoice = onStartVoice,
                    onStopVoice = onStopVoice,
                    onClearVoice = onClearVoice
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
