package com.example.ngepet.presentation.ui.screens

import com.example.ngepet.presentation.ui.*
import com.example.ngepet.presentation.ui.model.*
import com.example.ngepet.presentation.ui.theme.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

val chartColors = listOf(
    Green600, Pink400, Warning, Color(0xFF185FA5),
    Color(0xFFB4B2A9), Color(0xFF7B61FF), Color(0xFF56C596), Color(0xFFFF8A65)
)

@Composable
fun ReportScreen(transactions: List<TransactionUi>) {
    var selectedPeriod by remember { mutableStateOf("Bulanan") }
    var selectedSource by remember { mutableStateOf("Semua") }

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
        val matchesPeriod = txn.dateMillis in periodStart until periodEnd
        val matchesSource = selectedSource == "Semua" || txn.source == selectedSource
        matchesPeriod && matchesSource
    }

    val expenseTxs = filtered.filter { it.isExpense }
    val incomeTxs = filtered.filter { !it.isExpense }
    val totalExpense = expenseTxs.sumOf { it.amount }
    val totalIncome = incomeTxs.sumOf { it.amount }

    val expenseBreakdown = if (totalExpense > 0) {
        expenseTxs.groupBy { it.categoryName }.map { (name, txs) ->
            val amount = txs.sumOf { it.amount }
            CategoryBreakdown(categoryName = name, percentage = amount.toDouble() / totalExpense, amount = amount.toDouble())
        }.sortedByDescending { it.percentage }
    } else emptyList()

    val incomeBreakdown = if (totalIncome > 0) {
        incomeTxs.groupBy { it.categoryName }.map { (name, txs) ->
            val amount = txs.sumOf { it.amount }
            CategoryBreakdown(categoryName = name, percentage = amount.toDouble() / totalIncome, amount = amount.toDouble())
        }.sortedByDescending { it.percentage }
    } else emptyList()

    val dailyTotals = expenseTxs.groupBy { txn ->
        val cal = Calendar.getInstance().apply { timeInMillis = txn.dateMillis }
        cal.get(Calendar.DAY_OF_MONTH)
    }.mapValues { (_, txs) -> txs.sumOf { it.amount } }.toSortedMap()

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
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SourceChip("Semua", selectedSource == "Semua", onClick = { selectedSource = "Semua" })
                SourceChip("Manual", selectedSource == "Manual", onClick = { selectedSource = "Manual" })
            }
            Spacer(Modifier.height(18.dp))

            if (totalExpense > 0) {
                DonutChart(total = totalExpense, breakdown = expenseBreakdown, label = "Total keluar")
                Spacer(Modifier.height(12.dp))
                expenseBreakdown.forEachIndexed { index, item ->
                    val color = chartColors[index % chartColors.size]
                    ReportLegend(name = item.categoryName, percent = "${(item.percentage * 100).toInt()}%", amount = "Rp ${formatAmount(item.amount.toLong())}", color = color)
                }
                Spacer(Modifier.height(18.dp))
            }

            if (totalIncome > 0) {
                DonutChart(total = totalIncome, breakdown = incomeBreakdown, label = "Total pemasukan")
                Spacer(Modifier.height(12.dp))
                incomeBreakdown.forEachIndexed { index, item ->
                    val color = chartColors[(index + 4) % chartColors.size]
                    ReportLegend(name = item.categoryName, percent = "${(item.percentage * 100).toInt()}%", amount = "Rp ${formatAmount(item.amount.toLong())}", color = color)
                }
                Spacer(Modifier.height(18.dp))
            }

            if (dailyTotals.isNotEmpty()) {
                Text("Tren harian", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                DailyTrendChart(dailyTotals = dailyTotals, modifier = Modifier.fillMaxWidth().height(160.dp))
                Spacer(Modifier.height(18.dp))
            }

            if (totalExpense == 0L && totalIncome == 0L) {
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
                breakdown.forEachIndexed { index, item ->
                    val sweep = (item.percentage * 360).toFloat()
                    drawArc(chartColors[index % chartColors.size], startAngle, sweep, false, topLeft, chartSize, style = stroke)
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
fun DailyTrendChart(dailyTotals: Map<Int, Long>, modifier: Modifier = Modifier) {
    if (dailyTotals.isEmpty()) return

    val maxVal = dailyTotals.values.max().coerceAtLeast(1)
    val minDay = dailyTotals.keys.first()
    val maxDay = dailyTotals.keys.last()
    val dayRange = (maxDay - minDay).coerceAtLeast(1)

    Box(modifier = modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            val padding = 20.dp.toPx()
            val bottomPadding = 24.dp.toPx()
            val chartW = size.width - padding * 2
            val chartH = size.height - padding - bottomPadding

            val points = dailyTotals.map { (day, amount) ->
                val x = padding + (day - minDay).toFloat() / dayRange * chartW
                val y = padding + chartH - (amount.toFloat() / maxVal * chartH)
                Offset(x, y)
            }

            val gridLines = 4
            for (i in 0..gridLines) {
                val y = padding + chartH * i / gridLines
                drawLine(Color(0xFFE0E0E0), Offset(padding, y), Offset(size.width - padding, y), strokeWidth = 0.5f)
                val labelVal = maxVal * (gridLines - i) / gridLines
                drawContext.canvas.nativeCanvas.drawText(
                    "${labelVal / 1000}k", 0f, y + 4f,
                    android.graphics.Paint().apply { color = 0xFF888780.toInt(); textSize = 18f; textAlign = android.graphics.Paint.Align.LEFT }
                )
            }

            if (points.size >= 2) {
                val path = Path()
                path.moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    path.lineTo(points[i].x, points[i].y)
                }
                drawPath(path, Green600, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
            }

            points.forEach { point ->
                drawCircle(Color.White, 4.dp.toPx(), point)
                drawCircle(Green600, 3.dp.toPx(), point)
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
fun SourceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Green50 else CardSoft)
            .border(1.dp, if (selected) Green100 else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        color = if (selected) Green800 else Color(0xFF666666),
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
