package com.example.ngepet.presentation.ui.screens

import com.example.ngepet.presentation.ui.*
import com.example.ngepet.presentation.ui.model.*
import com.example.ngepet.presentation.ui.theme.*
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ngepet.presentation.ui.model.CategoryUi
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
import com.example.ngepet.presentation.ui.theme.Pink800
import java.util.Calendar

@Composable
fun AddTransactionSheet(
    mode: InputSheetMode,
    categories: List<CategoryUi>,
    editTxn: TransactionUi? = null,
    onModeChange: (InputSheetMode) -> Unit,
    onClose: () -> Unit,
    onSave: (Long, Long, String, Long, Boolean) -> Unit,
    onUpdate: ((String, Long, Long, String, Long, Boolean) -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxWidth().imePadding().navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp).verticalScroll(scrollState)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            val title = if (editTxn != null) "Edit transaksi" else if (mode == InputSheetMode.Manual) "Tambah transaksi" else "Tambah via suara"
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text("x", modifier = Modifier.clickable { onClose() }, color = Muted)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InputModeChip("Manual", Icons.Filled.Keyboard, mode == InputSheetMode.Manual, Modifier.weight(1f)) { onModeChange(InputSheetMode.Manual) }
            InputModeChip("Suara", Icons.Filled.Mic, mode == InputSheetMode.Voice, Modifier.weight(1f)) { onModeChange(InputSheetMode.Voice) }
        }
        Spacer(Modifier.height(12.dp))
        if (mode == InputSheetMode.Manual) {
            ManualInputContent(categories = categories, editTxn = editTxn, onSave = onSave, onUpdate = onUpdate)
        } else {
            PlaceholderVoiceContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputContent(
    categories: List<CategoryUi>,
    editTxn: TransactionUi?,
    onSave: (Long, Long, String, Long, Boolean) -> Unit,
    onUpdate: ((String, Long, Long, String, Long, Boolean) -> Unit)?
) {
    val isEditing = editTxn != null
    var amountRaw by remember { mutableStateOf(if (isEditing) editTxn!!.amount.toString() else "") }
    var note by remember { mutableStateOf(editTxn?.note ?: "") }
    var selectedType by remember { mutableStateOf(if (isEditing && !editTxn!!.isExpense) "Pemasukan" else "Pengeluaran") }
    var selectedCategory by remember { mutableStateOf(editTxn?.categoryId ?: categories.firstOrNull()?.id ?: "") }
    val initialCal = remember { Calendar.getInstance().apply { editTxn?.let { timeInMillis = it.dateMillis } } }
    val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    var selectedDateMillis by remember { mutableStateOf(initialCal.timeInMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(CardSoft).padding(3.dp)) {
        Box(Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
            .background(if (selectedType == "Pengeluaran") Color.White else Color.Transparent)
            .clickable { selectedType = "Pengeluaran" }.padding(8.dp), contentAlignment = Alignment.Center) {
            Text("Pengeluaran", color = if (selectedType == "Pengeluaran") Danger else Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
            .background(if (selectedType == "Pemasukan") Color.White else Color.Transparent)
            .clickable { selectedType = "Pemasukan" }.padding(8.dp), contentAlignment = Alignment.Center) {
            Text("Pemasukan", color = if (selectedType == "Pemasukan") Green600 else Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
    Spacer(Modifier.height(12.dp))
    Text("Kategori", color = Muted, fontSize = 11.sp)
    Spacer(Modifier.height(7.dp))
    CategoryGrid(categories = categories, selectedCategory = selectedCategory, onCategorySelected = { selectedCategory = it })
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Catatan") },
        placeholder = { Text("Contoh: Makan siang") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
    val amountDisplay = formatRupiah(amountRaw)
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(value = amountDisplay, onValueChange = { amountRaw = it.filter { c -> c.isDigit() } },
        label = { Text("Nominal") }, placeholder = { Text("0") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
        leadingIcon = { Text("Rp", color = Muted) }, shape = RoundedCornerShape(14.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Tanggal", color = Muted, fontSize = 11.sp)
        val calDisplay = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        val dateText = "${calDisplay.get(Calendar.DAY_OF_MONTH)} ${months[calDisplay.get(Calendar.MONTH)]} ${calDisplay.get(Calendar.YEAR)}"
        Text(dateText, color = Green600, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Green50).clickable { showDatePicker = true }.padding(horizontal = 12.dp, vertical = 6.dp))
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { selectedDateMillis = it }; showDatePicker = false }) { Text("Pilih", color = Green600) } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal", color = Muted) } }
        ) { DatePicker(state = datePickerState) }
    }
    Spacer(Modifier.height(10.dp))
    Button(
        onClick = {
            val amountValue = amountRaw.toLongOrNull() ?: 0L
            val catId = selectedCategory.toLongOrNull() ?: 0L
            if (isEditing && onUpdate != null) {
                onUpdate(editTxn!!.id, amountValue, catId, note, selectedDateMillis, selectedType == "Pengeluaran")
            } else {
                onSave(amountValue, catId, note, selectedDateMillis, selectedType == "Pengeluaran")
            }
        },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Green600),
        shape = RoundedCornerShape(13.dp),
        enabled = amountRaw.isNotBlank() && selectedCategory.isNotBlank()
    ) { Text(if (isEditing) "Simpan perubahan" else "Simpan transaksi") }
}

@Composable
fun CategoryGrid(categories: List<CategoryUi>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        categories.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { category ->
                    CategoryCard(category = category, selected = category.id == selectedCategory,
                        onClick = { onCategorySelected(category.id) }, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PlaceholderVoiceContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.size(80.dp).clip(CircleShape).background(Green50).border(2.dp, Green100, CircleShape), contentAlignment = Alignment.Center) {
            SymbolBox(Icons.Filled.Mic, Color.White, Green600, 54.dp)
        }
        Spacer(Modifier.height(10.dp))
        Text("Fitur suara akan segera hadir!", color = Muted, fontSize = 12.sp)
    }
}

@Composable
fun InputModeChip(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(modifier.clip(RoundedCornerShape(10.dp)).background(if (selected) Green50 else CardSoft)
        .border(1.dp, if (selected) Green100 else Color.Black.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
        .clickable { onClick() }.padding(8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = if (selected) Green800 else Color(0xFF666666), modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, color = if (selected) Green800 else Color(0xFF666666), fontSize = 12.sp)
    }
}

@Composable
fun CategoryCard(category: CategoryUi, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.clip(RoundedCornerShape(12.dp)).background(if (selected) Green50 else CardSoft)
        .border(1.dp, if (selected) Green100 else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
        .clickable { onClick() }.padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SymbolBox(iconForName(category.iconName), categoryColor(category.iconName), categoryBgColor(category.iconName), 30.dp)
        Text(category.name, color = if (selected) Green800 else Ink, fontSize = 11.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
    }
}
