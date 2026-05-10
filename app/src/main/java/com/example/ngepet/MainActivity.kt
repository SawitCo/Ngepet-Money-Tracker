package com.example.ngepet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NgepetTheme {
                NgepetApp()
            }
        }
    }
}

private enum class NgepetTab(val label: String, val icon: String) {
    Home("Home", "H"),
    History("Riwayat", "R"),
    Report("Laporan", "L"),
    Budget("Budget", "B")
}

private enum class InputSheetMode { Manual, Voice }

private data class TransactionUi(
    val title: String,
    val category: String,
    val amount: String,
    val isIncome: Boolean,
    val source: String,
    val icon: String,
    val color: Color,
    val bg: Color
)

private data class BudgetUi(
    val category: String,
    val period: String,
    val used: String,
    val limit: String,
    val progress: Float,
    val status: String,
    val icon: String,
    val color: Color,
    val bg: Color
)

private val recentTransactions = listOf(
    TransactionUi("Warung makan", "Makanan", "-Rp 25.000", false, "Suara", "M", Green600, Green50),
    TransactionUi("Grab ke kampus", "Transport", "-Rp 18.000", false, "Manual", "T", Color(0xFF185FA5), Color(0xFFE6F1FB)),
    TransactionUi("Gaji freelance", "Pekerjaan", "+Rp 500.000", true, "Manual", "P", Green600, Green50)
)

private val historyTransactions = recentTransactions + listOf(
    TransactionUi("Indomaret", "Belanja", "-Rp 47.000", false, "Suara", "B", Pink400, Pink50),
    TransactionUi("Tagihan listrik", "Tagihan", "-Rp 120.000", false, "Manual", "I", Warning, WarningBg),
    TransactionUi("Apotek Kimia Farma", "Kesehatan", "-Rp 35.000", false, "Suara", "K", Danger, DangerBg)
)

@Composable
private fun NgepetApp() {
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
                    NgepetTab.Home -> HomeScreen(onAddClick = { sheetMode = InputSheetMode.Manual })
                    NgepetTab.History -> HistoryScreen()
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
                onModeChange = { sheetMode = it },
                onClose = { sheetMode = null },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
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
private fun HomeScreen(onAddClick: () -> Unit) {
    ScreenColumn {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Selamat pagi,", color = Muted, fontSize = 11.sp)
                Text("Halo, Budi!", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
            SymbolBox("N", Green600, Green50, 36.dp)
        }
        Spacer(Modifier.height(14.dp))
        BalanceCard()
        Spacer(Modifier.height(12.dp))
        DailyTipCard()
        Spacer(Modifier.height(14.dp))
        SectionHeader("Transaksi terbaru", "Lihat semua")
        Spacer(Modifier.height(4.dp))
        recentTransactions.forEach { TransactionRow(it) }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Green600),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text("+ Catat sekarang")
        }
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
private fun DailyTipCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Green50)
            .padding(11.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        SymbolBox("!", Color.White, Green600, 30.dp)
        Column(Modifier.weight(1f)) {
            Text("Tip hari ini", color = Green800, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Coba alokasikan 20% pendapatanmu untuk tabungan darurat sebelum belanja.",
                color = Green800,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }
        Text("x", color = Green600, fontSize = 14.sp)
    }
}

@Composable
private fun HistoryScreen() {
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
        FilterChips(listOf("Semua" to true, "Suara" to true, "Manual" to false, "Makanan" to false, "Transport" to false))
        Spacer(Modifier.height(10.dp))
        LazyColumn {
            item { DateSeparator("Hari ini · 10 Mei") }
            items(historyTransactions.take(2)) { TransactionRow(it) }
            item { DateSeparator("Kemarin · 9 Mei") }
            items(historyTransactions.drop(2)) { TransactionRow(it) }
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
        BudgetUi("Makanan", "Harian", "Rp 499.000", "Rp 600.000", 0.83f, "Hampir habis", "M", Green600, Green50),
        BudgetUi("Belanja", "Bulanan", "Rp 209.000", "Rp 150.000", 1.39f, "Melebihi limit", "B", Pink400, Pink50),
        BudgetUi("Transport", "Bulanan", "Rp 162.000", "Rp 500.000", 0.32f, "Aman", "T", Color(0xFF185FA5), Color(0xFFE6F1FB)),
        BudgetUi("Tagihan", "Bulanan", "Rp 120.000", "Rp 200.000", 0.60f, "Aman", "I", Warning, WarningBg)
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
    onModeChange: (InputSheetMode) -> Unit,
    onClose: () -> Unit,
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
            InputModeChip("Manual", mode == InputSheetMode.Manual, Modifier.weight(1f)) { onModeChange(InputSheetMode.Manual) }
            InputModeChip("Suara", mode == InputSheetMode.Voice, Modifier.weight(1f)) { onModeChange(InputSheetMode.Voice) }
        }
        Spacer(Modifier.height(12.dp))
        if (mode == InputSheetMode.Manual) ManualInputContent() else VoiceInputContent()
    }
}

@Composable
private fun ManualInputContent() {
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
                .background(Color.White)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) { Text("Pengeluaran", color = Danger, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
        Box(modifier = Modifier.weight(1f).padding(8.dp), contentAlignment = Alignment.Center) {
            Text("Pemasukan", color = Muted, fontSize = 12.sp)
        }
    }
    Spacer(Modifier.height(12.dp))
    Text(
        "Rp",
        color = Muted,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        "25.000",
        color = Ink,
        fontSize = 34.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(10.dp))
    Text("Kategori", color = Muted, fontSize = 11.sp)
    Spacer(Modifier.height(7.dp))
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf("Makanan", "Pekerjaan", "Hiburan", "Transport", "Belanja", "Tagihan", "Lainnya").forEachIndexed { index, label ->
            CategoryPill(label, selected = index == 0)
        }
    }
    Spacer(Modifier.height(10.dp))
    FormField("Catatan", "Warung makan siang")
    FormField("Tanggal", "Hari ini, 10 Mei 2026")
    Button(
        onClick = {},
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Green600),
        shape = RoundedCornerShape(13.dp)
    ) { Text("Simpan transaksi") }
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
            SymbolBox("M", Color.White, Green600, 54.dp)
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
            Text("+", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Medium)
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
        Text(tab.icon, color = if (selected) Green600 else Muted, fontWeight = FontWeight.SemiBold)
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
private fun FilterChips(chips: List<Pair<String, Boolean>>) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        chips.forEachIndexed { index, chip ->
            val activeColor = if (index == 1) Pink50 else Green50
            val textColor = if (index == 1) Pink800 else Green800
            Text(
                chip.first,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (chip.second) activeColor else CardSoft)
                    .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
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
private fun InputModeChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Green50 else CardSoft)
            .border(1.dp, if (selected) Green100 else Color.Black.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Green800 else Color(0xFF666666), fontSize = 12.sp)
    }
}

@Composable
private fun CategoryPill(label: String, selected: Boolean) {
    Column(
        modifier = Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Green50 else CardSoft)
            .border(1.dp, if (selected) Green100 else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label.take(1), color = if (selected) Green600 else Muted, fontWeight = FontWeight.SemiBold)
        Text(label, color = if (selected) Green800 else Muted, fontSize = 9.sp, textAlign = TextAlign.Center)
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
private fun SymbolBox(text: String, color: Color, bg: Color, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true)
@Composable
private fun NgepetPreview() {
    NgepetTheme {
        NgepetApp()
    }
}
