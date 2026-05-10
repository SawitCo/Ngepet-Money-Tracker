package com.example.ngepet.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ngepet.presentation.ui.model.BudgetUi
import com.example.ngepet.presentation.ui.model.CategoryUi
import com.example.ngepet.presentation.ui.model.TransactionUi
import com.example.ngepet.presentation.ui.model.historyTransactions
import com.example.ngepet.presentation.ui.model.onboardingPages
import com.example.ngepet.presentation.ui.model.recentTransactions
import com.example.ngepet.presentation.ui.model.transactionCategories
import com.example.ngepet.ui.theme.CardSoft
import com.example.ngepet.ui.theme.Danger
import com.example.ngepet.ui.theme.DangerBg
import com.example.ngepet.ui.theme.Green100
import com.example.ngepet.ui.theme.Green50
import com.example.ngepet.ui.theme.Green600
import com.example.ngepet.ui.theme.Green800
import com.example.ngepet.ui.theme.Ink
import com.example.ngepet.ui.theme.Muted
import com.example.ngepet.ui.theme.NgepetTheme
import com.example.ngepet.ui.theme.Pink400
import com.example.ngepet.ui.theme.Pink50
import com.example.ngepet.ui.theme.Pink800
import com.example.ngepet.ui.theme.SurfaceWarm
import com.example.ngepet.ui.theme.Warning
import com.example.ngepet.ui.theme.WarningBg
import com.example.ngepet.ui.theme.WarningText

private enum class NgepetTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    History("Riwayat", Icons.AutoMirrored.Filled.List),
    Report("Laporan", Icons.Filled.PieChart),
    Budget("Budget", Icons.Filled.TrackChanges)
}

private enum class InputSheetMode { Manual, Voice }

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ngepet.presentation.MainViewModel
import com.example.ngepet.presentation.ui.model.CategoryUi
import java.util.Date

@Composable
fun NgepetApp(viewModel: MainViewModel = viewModel()) {
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState()
    val userName by viewModel.userName.collectAsState()
    
    val transactions by viewModel.transactions.collectAsState()
    val categoriesEntity by viewModel.categories.collectAsState()
    
    val categories = categoriesEntity.map {
        CategoryUi(id = it.id.toString(), name = it.name, iconName = it.iconName)
    }

    if (!hasCompletedOnboarding) {
        OnboardingScreen(
            initialName = userName ?: "",
            onComplete = { name ->
                viewModel.saveUserName(name.ifBlank { "Teman" })
            }
        )
        return
    }

    MainAppContent(
        userName = userName ?: "Teman",
        transactions = transactions,
        categories = categories,
        onAddTransaction = { amount, categoryId, note, isExpense ->
            viewModel.addTransaction(amount, categoryId, note, Date().time, isExpense)
        }
    )
}

@Composable
private fun MainAppContent(
    userName: String,
    transactions: List<com.example.ngepet.presentation.ui.model.TransactionUi>,
    categories: List<CategoryUi>,
    onAddTransaction: (Long, Long, String, Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf(NgepetTab.Home) }
    var sheetMode by remember { mutableStateOf<InputSheetMode?>(null) }

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
                when (selectedTab) {
                    NgepetTab.Home -> HomeScreen(userName = userName, transactions = transactions.take(5))
                    NgepetTab.History -> HistoryScreen(transactions = transactions)
                    NgepetTab.Report -> ReportScreen()
                    NgepetTab.Budget -> BudgetScreen()
                }
            }
        }

        sheetMode?.let { mode ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable { sheetMode = null }
            )
            AddTransactionSheet(
                mode = mode,
                categories = categories,
                onModeChange = { sheetMode = it },
                onClose = { sheetMode = null },
                onSave = { amount, categoryId, note, isExpense ->
                    onAddTransaction(amount, categoryId, note, isExpense)
                    sheetMode = null
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun OnboardingScreen(initialName: String, onComplete: (String) -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf(initialName) }
    val page = onboardingPages[currentPage]
    val isLastPage = currentPage == onboardingPages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWarm)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Ngepet", color = Green800, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Text("Ngedukasi Dompet", color = Muted, fontSize = 12.sp)
            }
            TextButton(onClick = { onComplete(name) }) {
                Text("Lewati", color = Green600)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(page.bg),
                contentAlignment = Alignment.Center
            ) {
                SymbolBox(page.icon, page.color, Color.White.copy(alpha = 0.72f), 86.dp)
            }
            Spacer(Modifier.height(28.dp))
            Text(
                page.title,
                color = Ink,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                page.description,
                color = Muted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            if (currentPage == 0) {
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Nama panggilan") },
                    placeholder = { Text("Contoh: Budi") },
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                onboardingPages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(width = if (index == currentPage) 22.dp else 7.dp, height = 7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (index == currentPage) Green600 else Green100)
                    )
                }
            }
            Button(
                onClick = {
                    if (isLastPage) onComplete(name) else currentPage += 1
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Green600),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(if (isLastPage) "Mulai pakai Ngepet" else "Lanjut")
            }
        }
    }
}

@Composable
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        content = content
    )
}

@Composable
private fun HomeScreen(userName: String, transactions: List<com.example.ngepet.presentation.ui.model.TransactionUi>) {
    var showDailyTip by remember { mutableStateOf(true) }

    ScreenColumn {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Selamat pagi,", color = Muted, fontSize = 11.sp)
                Text("Halo, $userName!", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
            SymbolBox(Icons.Filled.Notifications, Green600, Green50, 36.dp)
        }
        Spacer(Modifier.height(14.dp))
        BalanceCard()
        if (showDailyTip) {
            Spacer(Modifier.height(12.dp))
            DailyTipCard(onDismiss = { showDailyTip = false })
            Spacer(Modifier.height(14.dp))
        } else {
            Spacer(Modifier.height(14.dp))
        }
        SectionHeader("Transaksi terbaru", "Lihat semua")
        Spacer(Modifier.height(4.dp))
        transactions.forEach { TransactionRow(it) }
    }
}

@Composable
private fun BalanceCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Green600)
            .padding(16.dp)
    ) {
        Text("TOTAL SALDO", color = Color.White.copy(alpha = 0.72f), fontSize = 10.sp)
        Text("Rp 2.340.000", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BalanceMiniCard("Masuk bulan ini", "Rp 3.500.000", Modifier.weight(1f))
            BalanceMiniCard("Keluar bulan ini", "Rp 1.160.000", Modifier.weight(1f))
        }
    }
}

@Composable
private fun BalanceMiniCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(label, color = Color.White.copy(alpha = 0.72f), fontSize = 10.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DailyTipCard(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Green50)
            .padding(11.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        SymbolBox(Icons.Filled.Lightbulb, Color.White, Green600, 30.dp)
        Column(Modifier.weight(1f)) {
            Text("Tip hari ini", color = Green800, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Coba alokasikan 20% pendapatanmu untuk tabungan darurat sebelum belanja.",
                color = Green800,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }
        Text(
            "x",
            color = Green600,
            fontSize = 14.sp,
            modifier = Modifier.clickable { onDismiss() }
        )
    }
}

@Composable
private fun HistoryScreen(transactions: List<com.example.ngepet.presentation.ui.model.TransactionUi>) {
    var selectedSource by remember { mutableStateOf("Semua") }
    var selectedCategory by remember { mutableStateOf("Semua kategori") }
    val filteredTransactions = transactions.filter { transaction ->
        val matchesSource = selectedSource == "Semua" || transaction.note.contains(selectedSource, ignoreCase = true)
        val matchesCategory = selectedCategory == "Semua kategori" || transaction.categoryName == selectedCategory
        matchesSource && matchesCategory
    }

    ScreenColumn {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Riwayat", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text("Filter", color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
        HistoryFilters(
            selectedSource = selectedSource,
            selectedCategory = selectedCategory,
            onSourceSelected = { selectedSource = it },
            onCategorySelected = { selectedCategory = it }
        )
        Spacer(Modifier.height(10.dp))
        LazyColumn {
            if (filteredTransactions.isEmpty()) {
                item { EmptyState("Tidak ada transaksi yang cocok dengan filter ini") }
            } else {
                item { DateSeparator("Hasil filter") }
                items(filteredTransactions) { TransactionRow(it) }
            }
        }
    }
}

@Composable
private fun ReportScreen() {
    ScreenColumn {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Laporan", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text("Filter", color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PeriodChip("Harian", false)
            PeriodChip("Mingguan", false)
            PeriodChip("Bulanan", true)
        }
        Spacer(Modifier.height(10.dp))
        FilterChips(listOf("Semua" to true, "Suara" to true, "Manual" to false))
        Spacer(Modifier.height(18.dp))
        DonutChart()
        Spacer(Modifier.height(18.dp))
        ReportLegend("Makanan", "43%", "Rp 499.000", Green600)
        ReportLegend("Belanja", "18%", "Rp 209.000", Pink400)
        ReportLegend("Transport", "14%", "Rp 162.000", Warning)
        ReportLegend("Lainnya", "25%", "Rp 290.000", Color(0xFFB4B2A9))
    }
}

@Composable
private fun BudgetScreen() {
    val budgets = listOf(
        BudgetUi("Makanan", "Harian", "Rp 499.000", "Rp 600.000", 0.83f, "Hampir habis", Icons.Filled.Restaurant, Green600, Green50),
        BudgetUi("Belanja", "Bulanan", "Rp 209.000", "Rp 150.000", 1.39f, "Melebihi limit", Icons.Filled.ShoppingBag, Pink400, Pink50),
        BudgetUi("Transport", "Bulanan", "Rp 162.000", "Rp 500.000", 0.32f, "Aman", Icons.Filled.DirectionsBus, Color(0xFF185FA5), Color(0xFFE6F1FB)),
        BudgetUi("Tagihan", "Bulanan", "Rp 120.000", "Rp 200.000", 0.60f, "Aman", Icons.Filled.Receipt, Warning, WarningBg)
    )

    ScreenColumn {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Budget", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text("< Mei 2026 >", color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(budgets) { BudgetCard(it) }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Black.copy(alpha = 0.15f), RoundedCornerShape(13.dp))
                        .padding(13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+ Tambah budget baru", color = Green600, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AddTransactionSheet(
    mode: InputSheetMode,
    categories: List<CategoryUi>,
    onModeChange: (InputSheetMode) -> Unit,
    onClose: () -> Unit,
    onSave: (Long, Long, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .clickable(enabled = false) {}
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 34.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Black.copy(alpha = 0.12f))
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (mode == InputSheetMode.Manual) "Tambah transaksi" else "Tambah via suara", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text("x", modifier = Modifier.clickable { onClose() }, color = Muted)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InputModeChip("Manual", Icons.Filled.Keyboard, mode == InputSheetMode.Manual, Modifier.weight(1f)) { onModeChange(InputSheetMode.Manual) }
            InputModeChip("Suara", Icons.Filled.Mic, mode == InputSheetMode.Voice, Modifier.weight(1f)) { onModeChange(InputSheetMode.Voice) }
        }
        Spacer(Modifier.height(12.dp))
        if (mode == InputSheetMode.Manual) {
            ManualInputContent(categories, onSave)
        } else {
            VoiceInputContent()
        }
    }
}

@Composable
private fun ManualInputContent(categories: List<CategoryUi>, onSave: (Long, Long, String, Boolean) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Pengeluaran") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardSoft)
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (selectedType == "Pengeluaran") Color.White else Color.Transparent)
                .clickable { selectedType = "Pengeluaran" }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Pengeluaran",
                color = if (selectedType == "Pengeluaran") Danger else Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (selectedType == "Pemasukan") Color.White else Color.Transparent)
                .clickable { selectedType = "Pemasukan" }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Pemasukan",
                color = if (selectedType == "Pemasukan") Green600 else Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    Text("Kategori", color = Muted, fontSize = 11.sp)
    Spacer(Modifier.height(7.dp))
    CategoryGrid(
        categories = categories,
        selectedCategory = selectedCategory,
        onCategorySelected = { selectedCategory = it }
    )
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        label = { Text("Catatan", fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = amount,
        onValueChange = { amount = it },
        label = { Text("Nominal", fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Button(
        onClick = { 
            val amountValue = amount.toLongOrNull() ?: 0L
            val catId = selectedCategory.toLongOrNull() ?: 0L
            onSave(amountValue, catId, note, selectedType == "Pengeluaran") 
        },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Green600),
        shape = RoundedCornerShape(13.dp),
        enabled = amount.isNotBlank() && selectedCategory.isNotBlank()
    ) { Text("Simpan transaksi") }
}

@Composable
private fun CategoryGrid(categories: List<CategoryUi>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        categories.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { category ->
                    CategoryCard(
                        category = category,
                        selected = category.id == selectedCategory,
                        onClick = { onCategorySelected(category.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun VoiceInputContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Green50)
                .border(2.dp, Green100, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            SymbolBox(Icons.Filled.Mic, Color.White, Green600, 54.dp)
        }
        Spacer(Modifier.height(10.dp))
        WaveBars()
        Spacer(Modifier.height(10.dp))
        Text("Coba bilang:", color = Muted, fontSize = 11.sp)
        Text("\"Beli makan siang dua puluh ribu\"", color = Green800, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Green50)
                .padding(12.dp)
        ) {
            Text("HASIL DETEKSI", color = Green600, fontSize = 10.sp)
            ResultRow("Nominal", "Rp 25.000")
            ResultRow("Kategori", "Makanan")
            ResultRow("Tipe", "Pengeluaran")
        }
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green600),
            shape = RoundedCornerShape(13.dp)
        ) { Text("Konfirmasi & simpan") }
        Text("Tidak akurat? Edit manual", color = Pink800, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab: NgepetTab,
    onTabSelected: (NgepetTab) -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(NgepetTab.Home, selectedTab == NgepetTab.Home, onTabSelected)
        NavItem(NgepetTab.History, selectedTab == NgepetTab.History, onTabSelected)
        Box(
            modifier = Modifier
                .offset(y = (-12).dp)
                .size(50.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Green600)
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Tambah transaksi",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        NavItem(NgepetTab.Report, selectedTab == NgepetTab.Report, onTabSelected)
        NavItem(NgepetTab.Budget, selectedTab == NgepetTab.Budget, onTabSelected)
    }
}

@Composable
private fun NavItem(tab: NgepetTab, selected: Boolean, onTabSelected: (NgepetTab) -> Unit) {
    Column(
        modifier = Modifier.clickable { onTabSelected(tab) }.width(58.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = if (selected) Green600 else Muted,
            modifier = Modifier.size(21.dp)
        )
        Text(tab.label, color = if (selected) Green600 else Muted, fontSize = 10.sp)
    }
}

@Composable
private fun SectionHeader(title: String, action: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(action, color = Green600, fontSize = 11.sp)
    }
}

@Composable
private fun TransactionRow(item: TransactionUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        SymbolBox(item.icon, item.color, item.bg, 36.dp)
        Column(Modifier.weight(1f)) {
            Text(item.title, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(item.category, color = Muted, fontSize = 10.sp)
                SourceBadge(item.source)
            }
        }
        Text(item.amount, color = if (item.isIncome) Green600 else Danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SourceBadge(source: String) {
    val isVoice = source == "Suara"
    Text(
        source,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isVoice) Pink50 else Green50)
            .border(1.dp, if (isVoice) Pink400.copy(alpha = 0.25f) else Green100, RoundedCornerShape(20.dp))
            .padding(horizontal = 6.dp, vertical = 1.dp),
        color = if (isVoice) Pink800 else Green800,
        fontSize = 9.sp
    )
}

@Composable
private fun DateSeparator(text: String) {
    Text(text, color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SymbolBox(Icons.Filled.Receipt, Green600, Green50, 46.dp)
        Text(message, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun HistoryFilters(
    selectedSource: String,
    selectedCategory: String,
    onSourceSelected: (String) -> Unit,
    onCategorySelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterGroup(
            label = "Sumber input",
            chips = listOf("Semua", "Suara", "Manual"),
            selectedChip = selectedSource,
            accentChip = "Suara",
            onChipSelected = onSourceSelected
        )
        FilterGroup(
            label = "Kategori",
            chips = listOf(
                "Semua kategori",
                "Makanan",
                "Transport",
                "Belanja",
                "Tagihan",
                "Kesehatan"
            ),
            selectedChip = selectedCategory,
            onChipSelected = onCategorySelected
        )
    }
}

@Composable
private fun FilterGroup(
    label: String,
    chips: List<String>,
    selectedChip: String,
    accentChip: String? = null,
    onChipSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        FilterChips(
            chips = chips.map { it to (it == selectedChip) },
            accentIndex = chips.indexOf(accentChip),
            onChipSelected = onChipSelected
        )
    }
}

@Composable
private fun FilterChips(chips: List<Pair<String, Boolean>>) {
    FilterChips(chips = chips, accentIndex = 1)
}

@Composable
private fun FilterChips(
    chips: List<Pair<String, Boolean>>,
    accentIndex: Int,
    onChipSelected: ((String) -> Unit)? = null
) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        chips.forEachIndexed { index, chip ->
            val activeColor = if (index == accentIndex) Pink50 else Green50
            val textColor = if (index == accentIndex) Pink800 else Green800
            Text(
                chip.first,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (chip.second) activeColor else CardSoft)
                    .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .clickable(enabled = onChipSelected != null) { onChipSelected?.invoke(chip.first) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = if (chip.second) textColor else Color(0xFF666666),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PeriodChip(label: String, selected: Boolean) {
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Green600 else Color.Transparent)
            .border(1.dp, if (selected) Green600 else Color.Black.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        color = if (selected) Color.White else Muted,
        fontSize = 11.sp
    )
}

@Composable
private fun DonutChart() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val stroke = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round)
            val chartSize = Size(size.width - 28.dp.toPx(), size.height - 28.dp.toPx())
            val topLeft = Offset(14.dp.toPx(), 14.dp.toPx())
            drawArc(Green50, -90f, 360f, false, topLeft, chartSize, style = stroke)
            drawArc(Green600, -90f, 155f, false, topLeft, chartSize, style = stroke)
            drawArc(Pink400, 70f, 65f, false, topLeft, chartSize, style = stroke)
            drawArc(Warning, 140f, 50f, false, topLeft, chartSize, style = stroke)
            drawArc(Color(0xFFB4B2A9), 195f, 90f, false, topLeft, chartSize, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total keluar", color = Muted, fontSize = 10.sp)
            Text("Rp 1,16jt", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ReportLegend(name: String, percent: String, amount: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(Modifier.width(8.dp))
        Text(name, modifier = Modifier.weight(1f), fontSize = 13.sp)
        Text(percent, color = Muted, fontSize = 11.sp)
        Spacer(Modifier.width(8.dp))
        Text(amount, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BudgetCard(item: BudgetUi) {
    val isOver = item.progress >= 1f
    val isWarn = item.progress >= 0.7f && !isOver
    val barColor = when {
        isOver -> Color(0xFFE24B4A)
        isWarn -> Warning
        else -> Green600
    }
    val badgeBg = when {
        isOver -> DangerBg
        isWarn -> WarningBg
        else -> Green50
    }
    val badgeText = when {
        isOver -> Danger
        isWarn -> WarningText
        else -> Green800
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(CardSoft)
            .then(if (isOver) Modifier.border(1.dp, Color(0xFFF7C1C1), RoundedCornerShape(13.dp)) else Modifier)
            .padding(11.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SymbolBox(item.icon, item.color, item.bg, 32.dp)
                Column {
                    Text(item.category, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(item.period, color = Muted, fontSize = 10.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(item.used, color = if (isOver) Danger else Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("dari ${item.limit}", color = Muted, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        ProgressBar(item.progress.coerceAtMost(1f), barColor)
        Spacer(Modifier.height(7.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(if (isOver) "+Rp 59.000 melebihi limit" else "${(item.progress * 100).toInt()}% terpakai", color = if (isOver) Danger else Muted, fontSize = 10.sp)
            Text(
                item.status,
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(badgeBg).padding(horizontal = 8.dp, vertical = 3.dp),
                color = badgeText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color.Black.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
    }
}

@Composable
private fun InputModeChip(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Green50 else CardSoft)
            .border(1.dp, if (selected) Green100 else Color.Black.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Green800 else Color(0xFF666666),
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(label, color = if (selected) Green800 else Color(0xFF666666), fontSize = 12.sp)
    }
}

@Composable
private fun CategoryCard(
    category: CategoryUi,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .then(modifier)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Green50 else CardSoft)
            .border(1.dp, if (selected) Green100 else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SymbolBox(category.icon, category.color, category.bg, 30.dp)
        Text(
            category.label,
            color = if (selected) Green800 else Ink,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun FormField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 7.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CardSoft)
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(label, color = Muted, fontSize = 10.sp)
        Text(value, color = Ink, fontSize = 13.sp)
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, fontSize = 11.sp)
        Text(value, color = Green800, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun WaveBars() {
    val bars = listOf(10.dp, 20.dp, 14.dp, 26.dp, 10.dp, 18.dp, 8.dp, 22.dp, 12.dp)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        bars.forEachIndexed { index, height ->
            Box(
                Modifier
                    .width(4.dp)
                    .height(height)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (index % 2 == 1) Pink400 else Green600)
            )
        }
    }
}

@Composable
private fun SymbolBox(icon: ImageVector, color: Color, bg: Color, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NgepetPreview() {
    NgepetTheme {
        NgepetApp()
    }
}
