package com.example.ngepet.presentation.ui.screens

import com.example.ngepet.presentation.ui.*
import com.example.ngepet.presentation.ui.model.*
import com.example.ngepet.presentation.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ngepet.presentation.ui.model.TransactionUi
import com.example.ngepet.presentation.ui.theme.CardSoft
import com.example.ngepet.presentation.ui.theme.Green50
import com.example.ngepet.presentation.ui.theme.Green600
import com.example.ngepet.presentation.ui.theme.Green800
import com.example.ngepet.presentation.ui.theme.Muted
import com.example.ngepet.presentation.ui.theme.Pink50
import com.example.ngepet.presentation.ui.theme.Pink800

@Composable
fun HistoryScreen(transactions: List<TransactionUi>) {
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
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
fun HistoryFilters(
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
fun FilterGroup(
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
fun FilterChips(chips: List<Pair<String, Boolean>>) {
    FilterChips(chips = chips, accentIndex = 1)
}

@Composable
fun FilterChips(
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
fun DateSeparator(text: String) {
    Text(text, color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
}
