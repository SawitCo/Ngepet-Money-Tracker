package com.example.ngepet.presentation.ui.screens

import com.example.ngepet.presentation.ui.*
import com.example.ngepet.presentation.ui.model.*
import com.example.ngepet.presentation.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ngepet.presentation.ui.model.BudgetUi
import com.example.ngepet.presentation.ui.model.CategoryUi
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
fun BudgetScreen(
    budgets: List<BudgetUi>,
    categories: List<CategoryUi>,
    onAddBudget: (Long, Long) -> Unit,
    onUpdateBudget: (String, Long) -> Unit,
    onDeleteBudget: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editBudgetId by remember { mutableStateOf<String?>(null) }

    ScreenColumn {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Budget", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text("< Mei 2026 >", color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
        if (budgets.isEmpty()) {
            EmptyState("Belum ada budget. Tambah budget baru untuk mulai mengatur pengeluaran!", icon = Icons.Filled.TrackChanges)
            Spacer(Modifier.weight(1f))
            AddBudgetButton { showAddDialog = true }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(budgets) { budget ->
                    BudgetCard(item = budget, onEdit = { editBudgetId = budget.id }, onDelete = { onDeleteBudget(budget.id) })
                }
                item { AddBudgetButton { showAddDialog = true } }
            }
        }
    }

    if (showAddDialog) {
        BudgetDialog(title = "Tambah Budget Baru", categories = categories,
            onDismiss = { showAddDialog = false },
            onSave = { catId, limit -> onAddBudget(catId, limit); showAddDialog = false })
    }

    editBudgetId?.let { id ->
        val budget = budgets.find { it.id == id }
        if (budget != null) {
            val cat = categories.find { it.id == budget.categoryId }
            BudgetDialog(title = "Edit Budget ${cat?.name ?: ""}", categories = categories,
                initialCategoryId = budget.categoryId, initialLimit = budget.limitAmount,
                onDismiss = { editBudgetId = null },
                onSave = { _, limit -> onUpdateBudget(id, limit); editBudgetId = null })
        } else { editBudgetId = null }
    }
}

@Composable
private fun AddBudgetButton(onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
        .border(1.dp, Color.Black.copy(alpha = 0.15f), RoundedCornerShape(13.dp))
        .clickable { onClick() }.padding(13.dp), contentAlignment = Alignment.Center) {
        Text("+ Tambah budget baru", color = Green600, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BudgetCard(item: BudgetUi, onEdit: () -> Unit, onDelete: () -> Unit) {
    val isOver = item.progress >= 1f
    val isWarn = item.progress >= 0.7f && !isOver
    val barColor = when { isOver -> Color(0xFFE24B4A); isWarn -> Warning; else -> Green600 }
    val badgeBg = when { isOver -> DangerBg; isWarn -> WarningBg; else -> Green50 }
    val badgeText = when { isOver -> Danger; isWarn -> WarningText; else -> Green800 }
    val icon = iconForName(item.iconName)

    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(CardSoft)
        .then(if (isOver) Modifier.border(1.dp, Color(0xFFF7C1C1), RoundedCornerShape(13.dp)) else Modifier)
        .clickable { onEdit() }.padding(11.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SymbolBox(icon, categoryColor(item.iconName), categoryBgColor(item.iconName), 32.dp)
                Column { Text(item.category, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium); Text(item.period, color = Muted, fontSize = 10.sp) }
            }
            Text("x", color = Muted, fontSize = 14.sp, modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onDelete() }.padding(4.dp).semantics { contentDescription = "Hapus budget" })
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text(item.used, color = if (isOver) Danger else Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold); Text("dari ${item.limit}", color = Muted, fontSize = 10.sp) }
        }
        Spacer(Modifier.height(10.dp))
        ProgressBar(item.progress.coerceAtMost(1f), barColor)
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            val overText = if (isOver && item.overAmount > 0) "+Rp ${formatAmount(item.overAmount)} melebihi limit" else "${(item.progress * 100).toInt()}% terpakai"
            Text(overText, color = if (isOver) Danger else Muted, fontSize = 10.sp)
            Text(item.status, modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(badgeBg).padding(horizontal = 8.dp, vertical = 3.dp), color = badgeText, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProgressBar(progress: Float, color: Color) {
    Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color.Black.copy(alpha = 0.08f))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(progress).clip(RoundedCornerShape(3.dp)).background(color))
    }
}

@Composable
fun BudgetDialog(title: String, categories: List<CategoryUi>, initialCategoryId: String? = null, initialLimit: Long = 0, onDismiss: () -> Unit, onSave: (Long, Long) -> Unit) {
    var selectedCategory by remember { mutableStateOf(initialCategoryId ?: "") }
    var limitRaw by remember { mutableStateOf(if (initialLimit > 0) initialLimit.toString() else "") }

    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).padding(20.dp).verticalScroll(rememberScrollState())) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("x", color = Muted, modifier = Modifier.clickable { onDismiss() }.semantics { contentDescription = "Tutup" })
            }
            Spacer(Modifier.height(14.dp))
            Text("Kategori", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(7.dp))
            CategoryGrid(categories = categories, selectedCategory = selectedCategory, onCategorySelected = { selectedCategory = it })
            Spacer(Modifier.height(10.dp))
            val limitDisplay = formatRupiah(limitRaw)
            OutlinedTextField(value = limitDisplay, onValueChange = { limitRaw = it.filter { c -> c.isDigit() } }, label = { Text("Batas budget") },
                placeholder = { Text("0") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Text("Rp", color = Muted) },
                shape = RoundedCornerShape(14.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(Modifier.height(14.dp))
            Button(onClick = { val catId = selectedCategory.toLongOrNull() ?: return@Button; val limit = limitRaw.toLongOrNull() ?: return@Button; if (limit <= 0) return@Button; onSave(catId, limit) },
                modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Green600), shape = RoundedCornerShape(13.dp),
                enabled = selectedCategory.isNotBlank() && limitRaw.isNotBlank() && (limitRaw.toLongOrNull() ?: 0) > 0) { Text("Simpan") }
        }
    }
}
