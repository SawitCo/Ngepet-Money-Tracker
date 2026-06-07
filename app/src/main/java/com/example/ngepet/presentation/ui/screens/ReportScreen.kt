package com.example.ngepet.presentation.ui.screens

import com.example.ngepet.presentation.ui.*
import com.example.ngepet.presentation.ui.model.*
import com.example.ngepet.presentation.ui.theme.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ngepet.domain.model.CategoryBreakdown
import com.example.ngepet.presentation.ui.model.TransactionUi
import com.example.ngepet.presentation.ui.theme.Green50
import com.example.ngepet.presentation.ui.theme.Green600
import com.example.ngepet.presentation.ui.theme.Ink
import com.example.ngepet.presentation.ui.theme.Muted
import com.example.ngepet.presentation.ui.theme.Pink400
import com.example.ngepet.presentation.ui.theme.Warning
import java.util.Calendar

@Composable
fun ReportScreen(transactions: List<TransactionUi>) {
    var selectedPeriod by remember { mutableStateOf("Bulanan") }
    var showExpense by remember { mutableStateOf(true) }

    val now = Calendar.getInstance()
    val periodStart: Long
    val periodEnd: Long

    when (selectedPeriod) {
        "Harian" -> {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            periodStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, 1)
            periodEnd = cal.timeInMillis
        }
        "Mingguan" -> {
            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            periodStart = cal.timeInMillis
            cal.add(Calendar.WEEK_OF_YEAR, 1)
            periodEnd = cal.timeInMillis
        }
        else -> {
            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            periodStart = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            periodEnd = cal.timeInMillis
        }
    }

    val filtered = transactions.filter { txn ->
        txn.dateMillis in periodStart until periodEnd
    }

    val expenseTxs = filtered.filter { it.isExpense }
    val incomeTxs = filtered.filter { !it.isExpense }
    val totalExpense = expenseTxs.sumOf { it.amount }
    val totalIncome = incomeTxs.sumOf { it.amount }

    val currentTxs = if (showExpense) expenseTxs else incomeTxs
    val currentTotal = if (showExpense) totalExpense else totalIncome
    val currentLabel = if (showExpense) "Total keluar" else "Total pemasukan"

    val currentBreakdown = if (currentTotal > 0) {
        currentTxs.groupBy { it.categoryName }.map { (name, txs) ->
            val amount = txs.sumOf { it.amount }
            val icon = txs.first().categoryIcon
            CategoryBreakdown(categoryName = name, percentage = amount.toDouble() / currentTotal, amount = amount.toDouble(), colorHex = icon)
        }.sortedByDescending { it.percentage }
    } else emptyList()

    val dailyExpenses = expenseTxs.groupBy { txn ->
        val cal = Calendar.getInstance().apply { timeInMillis = txn.dateMillis }
        cal.get(Calendar.DAY_OF_MONTH)
    }.mapValues { (_, txs) -> txs.sumOf { it.amount } }.toSortedMap()

    val dailyIncome = incomeTxs.groupBy { txn ->
        val cal = Calendar.getInstance().apply { timeInMillis = txn.dateMillis }
        cal.get(Calendar.DAY_OF_MONTH)
    }.mapValues { (_, txs) -> txs.sumOf { it.amount } }.toSortedMap()

    val hasChartData = dailyExpenses.isNotEmpty() || dailyIncome.isNotEmpty()

    ScreenColumn {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                PeriodChip("Harian", selectedPeriod == "Harian", onClick = { selectedPeriod = "Harian" })
                PeriodChip("Mingguan", selectedPeriod == "Mingguan", onClick = { selectedPeriod = "Mingguan" })
                PeriodChip("Bulanan", selectedPeriod == "Bulanan", onClick = { selectedPeriod = "Bulanan" })
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (showExpense) "Pengeluaran" else "Pemasukan", color = if (showExpense) Color(0xFFA32D2D) else Green600, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = showExpense,
                    onCheckedChange = { showExpense = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Green600, uncheckedTrackColor = Color(0xFFA32D2D).copy(alpha = 0.3f), checkedThumbColor = Color.White, uncheckedThumbColor = Color.White)
                )
            }
            Spacer(Modifier.height(18.dp))

            if (currentTotal > 0) {
                DonutChart(total = currentTotal, breakdown = currentBreakdown, label = currentLabel)
                Spacer(Modifier.height(12.dp))
                currentBreakdown.forEach { item ->
                    ReportLegend(name = item.categoryName, percent = "${(item.percentage * 100).toInt()}%", amount = "Rp ${formatAmount(item.amount.toLong())}", color = categoryColor(item.colorHex))
                }
                Spacer(Modifier.height(18.dp))
            }

            if (hasChartData) {
                Text("Tren harian", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFE53935)))
                        Text("Pengeluaran", color = Muted, fontSize = 10.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Green600))
                        Text("Pemasukan", color = Muted, fontSize = 10.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                DailyTrendChart(
                    dailyExpenses = dailyExpenses,
                    dailyIncome = dailyIncome,
                    periodDays = ((periodEnd - periodStart) / 86400000L).toInt().coerceAtLeast(1),
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
                Spacer(Modifier.height(18.dp))
            }

            if (currentTotal == 0L) {
                EmptyState("Belum cukup data untuk laporan periode ini")
            }
        }
    }
}

@Composable
fun DonutChart(total: Long, breakdown: List<CategoryBreakdown>, label: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val stroke = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round)
            val chartSize = Size(size.width - 28.dp.toPx(), size.height - 28.dp.toPx())
            val topLeft = Offset(14.dp.toPx(), 14.dp.toPx())

            if (breakdown.isEmpty()) {
                drawArc(Green50, -90f, 360f, false, topLeft, chartSize, style = stroke)
            } else {
                var startAngle = -90f
                breakdown.forEach { item ->
                    val sweep = (item.percentage * 360).toFloat()
                    drawArc(categoryColor(item.colorHex), startAngle, sweep, false, topLeft, chartSize, style = stroke)
                    startAngle += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Muted, fontSize = 10.sp)
            val displayAmount = if (total >= 1_000_000) {
                val juta = total / 1_000_000
                val sisa = (total % 1_000_000) / 100_000
                "Rp ${juta},${sisa}jt"
            } else {
                "Rp ${formatAmount(total)}"
            }
            Text(displayAmount, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun DailyTrendChart(dailyExpenses: Map<Int, Long>, dailyIncome: Map<Int, Long>, periodDays: Int, modifier: Modifier = Modifier) {
    val allDays = (dailyExpenses.keys + dailyIncome.keys).sorted()
    if (allDays.isEmpty()) return

    val allValues = dailyExpenses.values + dailyIncome.values
    val maxVal = allValues.max().coerceAtLeast(1)
    val minDay = allDays.first()
    val maxDay = allDays.last()
    val dayRange = (maxDay - minDay).coerceAtLeast(1)

    val xLabelCount = when {
        periodDays <= 3 -> periodDays + 1
        periodDays <= 10 -> 5
        periodDays <= 20 -> 4
        else -> 5
    }

    val expenseColor = Color(0xFFE53935)
    val incomeColor = Green600

    Box(modifier = modifier.offset(x = (-16).dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            val topPadding = 12.dp.toPx()
            val bottomPadding = 28.dp.toPx()
            val labelArea = 48.dp.toPx()
            val gap = 12.dp.toPx()
            val chartW = size.width - labelArea - gap
            val chartH = size.height - topPadding - bottomPadding

            val gridLines = 4
            for (i in 0..gridLines) {
                val y = topPadding + chartH * i / gridLines
                drawLine(Color(0xFFCCCCCC), Offset(labelArea + gap, y), Offset(size.width, y), strokeWidth = 1f)
                val labelVal = maxVal * (gridLines - i) / gridLines
                drawContext.canvas.nativeCanvas.drawText(
                    if (labelVal >= 1_000_000) "${labelVal / 1_000_000}jt" else if (labelVal >= 1_000) "${labelVal / 1_000}k" else "$labelVal",
                    labelArea, y + 5f,
                    android.graphics.Paint().apply { color = 0xFF888780.toInt(); textSize = 20f; textAlign = android.graphics.Paint.Align.RIGHT }
                )
            }

            fun drawLine(data: Map<Int, Long>, color: Color) {
                val pts = data.map { (day, amount) ->
                    val x = labelArea + gap + (day - minDay).toFloat() / dayRange * chartW
                    val y = topPadding + chartH - (amount.toFloat() / maxVal * chartH)
                    Offset(x, y)
                }
                if (pts.size >= 2) {
                    val path = Path()
                    path.moveTo(pts[0].x, pts[0].y)
                    for (i in 1 until pts.size) {
                        path.lineTo(pts[i].x, pts[i].y)
                    }
                    drawPath(path, color, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
                }
                pts.forEach { pt ->
                    drawCircle(Color.White, 5.dp.toPx(), pt)
                    drawCircle(color, 4.dp.toPx(), pt)
                }
            }

            drawLine(dailyExpenses, expenseColor)
            drawLine(dailyIncome, incomeColor)

            for (i in 0..xLabelCount) {
                val day = minDay + dayRange * i / xLabelCount
                val x = labelArea + gap + (day - minDay).toFloat() / dayRange * chartW
                drawContext.canvas.nativeCanvas.drawText(
                    "$day", x, size.height - 4.dp.toPx(),
                    android.graphics.Paint().apply { color = 0xFF888780.toInt(); textSize = 18f; textAlign = android.graphics.Paint.Align.CENTER }
                )
            }
        }
    }
}

@Composable
fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Green600 else Color.Transparent)
            .border(1.dp, if (selected) Green600 else Color.Black.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        color = if (selected) Color.White else Muted,
        fontSize = 11.sp
    )
}

@Composable
fun ReportLegend(name: String, percent: String, amount: String, color: Color) {
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
