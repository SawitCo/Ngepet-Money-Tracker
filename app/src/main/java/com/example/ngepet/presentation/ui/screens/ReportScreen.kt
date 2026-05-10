package com.example.ngepet.presentation.ui.screens

import com.example.ngepet.presentation.ui.*
import com.example.ngepet.presentation.ui.model.*
import com.example.ngepet.presentation.ui.theme.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ngepet.presentation.ui.theme.Green50
import com.example.ngepet.presentation.ui.theme.Green600
import com.example.ngepet.presentation.ui.theme.Green800
import com.example.ngepet.presentation.ui.theme.Ink
import com.example.ngepet.presentation.ui.theme.Muted
import com.example.ngepet.presentation.ui.theme.Pink400
import com.example.ngepet.presentation.ui.theme.Warning

@Composable
fun ReportScreen() {
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
fun PeriodChip(label: String, selected: Boolean) {
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
fun DonutChart() {
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
