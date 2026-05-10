package com.example.ngepet.presentation.ui.screens

import com.example.ngepet.presentation.ui.*
import com.example.ngepet.presentation.ui.model.*
import com.example.ngepet.presentation.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ngepet.presentation.ui.model.BudgetUi
import com.example.ngepet.presentation.ui.theme.CardSoft
import com.example.ngepet.presentation.ui.theme.Danger
import com.example.ngepet.presentation.ui.theme.DangerBg
import com.example.ngepet.presentation.ui.theme.Green50
import com.example.ngepet.presentation.ui.theme.Green600
import com.example.ngepet.presentation.ui.theme.Green800
import com.example.ngepet.presentation.ui.theme.Ink
import com.example.ngepet.presentation.ui.theme.Muted
import com.example.ngepet.presentation.ui.theme.Pink400
import com.example.ngepet.presentation.ui.theme.Pink50
import com.example.ngepet.presentation.ui.theme.Warning
import com.example.ngepet.presentation.ui.theme.WarningBg
import com.example.ngepet.presentation.ui.theme.WarningText

@Composable
fun BudgetScreen() {
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
fun BudgetCard(item: BudgetUi) {
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
fun ProgressBar(progress: Float, color: Color) {
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
