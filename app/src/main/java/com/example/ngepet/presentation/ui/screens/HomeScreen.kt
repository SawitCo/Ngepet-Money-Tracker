package com.example.ngepet.presentation.ui.screens

import com.example.ngepet.presentation.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ngepet.presentation.ui.model.TransactionUi
import com.example.ngepet.presentation.ui.theme.Green50
import com.example.ngepet.presentation.ui.theme.Green600
import com.example.ngepet.presentation.ui.theme.Green800
import com.example.ngepet.presentation.ui.theme.Ink
import com.example.ngepet.presentation.ui.theme.Muted

@Composable
fun HomeScreen(
    userName: String,
    transactions: List<TransactionUi>,
    currentBalance: Long,
    monthlyIncome: Long,
    monthlyExpense: Long,
    currentTip: String
) {
    var showDailyTip by remember { mutableStateOf(true) }

    ScreenColumn {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Halo, $userName!", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            }
            SymbolBox(Icons.Filled.Notifications, Green600, Green50, 36.dp, contentDescription = "Notifikasi")
        }
        Spacer(Modifier.height(14.dp))
        BalanceCard(currentBalance = currentBalance, monthlyIncome = monthlyIncome, monthlyExpense = monthlyExpense)
        if (showDailyTip) {
            Spacer(Modifier.height(12.dp))
            DailyTipsCard(tip = currentTip, onDismiss = { showDailyTip = false })
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
fun BalanceCard(currentBalance: Long, monthlyIncome: Long, monthlyExpense: Long) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Green600)
            .padding(16.dp)
    ) {
        Text("TOTAL SALDO", color = Color.White.copy(alpha = 0.72f), fontSize = 10.sp)
        Text("Rp ${formatAmount(currentBalance)}", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BalanceMiniCard("Masuk bulan ini", "Rp ${formatAmount(monthlyIncome)}", Modifier.weight(1f))
            BalanceMiniCard("Keluar bulan ini", "Rp ${formatAmount(monthlyExpense)}", Modifier.weight(1f))
        }
    }
}

@Composable
fun BalanceMiniCard(label: String, value: String, modifier: Modifier = Modifier) {
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
fun DailyTipsCard(tip: String, onDismiss: () -> Unit) {
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
            Text("Tips hari ini", color = Green800, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(
                tip,
                color = Green800,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }
        Icon(
            Icons.Filled.Close,
            contentDescription = "Tutup tips",
            tint = Green600,
            modifier = Modifier.size(18.dp).clickable { onDismiss() }
        )
    }
}
