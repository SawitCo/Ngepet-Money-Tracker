package com.example.ngepet.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ngepet.presentation.ui.model.TransactionUi
import com.example.ngepet.presentation.ui.theme.CardSoft
import com.example.ngepet.presentation.ui.theme.Danger
import com.example.ngepet.presentation.ui.theme.Green100
import com.example.ngepet.presentation.ui.theme.Green50
import com.example.ngepet.presentation.ui.theme.Green600
import com.example.ngepet.presentation.ui.theme.Green800
import com.example.ngepet.presentation.ui.theme.Ink
import com.example.ngepet.presentation.ui.theme.Muted
import com.example.ngepet.presentation.ui.theme.Pink400
import com.example.ngepet.presentation.ui.theme.Pink50
import com.example.ngepet.presentation.ui.theme.Pink800
import com.example.ngepet.presentation.ui.theme.SurfaceWarm

enum class NgepetTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    History("Riwayat", Icons.AutoMirrored.Filled.List),
    Report("Laporan", Icons.Filled.PieChart),
    Budget("Budget", Icons.Filled.TrackChanges)
}

enum class InputSheetMode { Manual, Voice }

@Composable
fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        content = content
    )
}

@Composable
fun SectionHeader(title: String, action: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(action, color = Green600, fontSize = 11.sp)
    }
}

@Composable
fun EmptyState(message: String) {
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
fun SymbolBox(icon: ImageVector, color: Color, bg: Color, size: Dp) {
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

@Composable
fun SourceBadge(source: String) {
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
fun TransactionRow(item: TransactionUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        SymbolBox(iconForName(item.categoryIcon), Green600, Green50, 36.dp)
        Column(Modifier.weight(1f)) {
            Text(item.note, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(item.categoryName, color = Muted, fontSize = 10.sp)
                SourceBadge(item.source)
            }
        }
        Text("Rp ${formatRupiah(item.amount.toString())}", color = if (!item.isExpense) Green600 else Danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BottomNavigationBar(
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
fun NavItem(tab: NgepetTab, selected: Boolean, onTabSelected: (NgepetTab) -> Unit) {
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

fun formatAmount(amount: Long): String {
    return String.format("%,d", amount).replace(",", ".")
}

fun iconForName(name: String): ImageVector {
    return when (name) {
        "Restaurant" -> Icons.Filled.Restaurant
        "Commute" -> Icons.Filled.DirectionsBus
        "Payments" -> Icons.Filled.Work
        "ShoppingCart" -> Icons.Filled.ShoppingBag
        "Movie" -> Icons.Filled.Favorite
        "Receipt" -> Icons.Filled.Receipt
        "LocalHospital" -> Icons.Filled.Favorite
        "MoreHoriz" -> Icons.Filled.MoreHoriz
        "Wallet" -> Icons.Filled.AccountBalanceWallet
        else -> Icons.Filled.Receipt
    }
}

fun formatRupiah(amount: String): String {
    val digits = amount.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    return digits.reversed().chunked(3).joinToString(".").reversed()
}
