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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ngepet.presentation.ui.model.CategoryUi
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
import com.example.ngepet.presentation.VoiceResult

@Composable
fun AddTransactionSheet(
    mode: InputSheetMode,
    categories: List<CategoryUi>,
    voiceResult: VoiceResult?,
    isListening: Boolean,
    voiceError: String?,
    onModeChange: (InputSheetMode) -> Unit,
    onClose: () -> Unit,
    onSave: (Long, Long, String, Boolean) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onClearVoice: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .verticalScroll(scrollState)
    ) {
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
            InputModeChip("Manual", Icons.Filled.Keyboard, mode == InputSheetMode.Manual, Modifier.weight(1f)) { onModeChange(InputSheetMode.Manual) }
            InputModeChip("Suara", Icons.Filled.Mic, mode == InputSheetMode.Voice, Modifier.weight(1f)) { onModeChange(InputSheetMode.Voice) }
        }
        Spacer(Modifier.height(12.dp))
        if (mode == InputSheetMode.Manual) {
            ManualInputContent(categories, onSave)
        } else {
            VoiceInputContent(
                voiceResult = voiceResult,
                isListening = isListening,
                voiceError = voiceError,
                onStartVoice = onStartVoice,
                onStopVoice = onStopVoice,
                onClearVoice = onClearVoice,
                categories = categories,
                onSave = onSave
            )
        }
    }
}

@Composable
fun ManualInputContent(categories: List<CategoryUi>, onSave: (Long, Long, String, Boolean) -> Unit) {
    var amountRaw by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Pengeluaran") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }

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
                .background(if (selectedType == "Pengeluaran") Color.White else Color.Transparent)
                .clickable { selectedType = "Pengeluaran" }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Pengeluaran",
                color = if (selectedType == "Pengeluaran") Danger else Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (selectedType == "Pemasukan") Color.White else Color.Transparent)
                .clickable { selectedType = "Pemasukan" }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Pemasukan",
                color = if (selectedType == "Pemasukan") Green600 else Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    Text("Kategori", color = Muted, fontSize = 11.sp)
    Spacer(Modifier.height(7.dp))
    CategoryGrid(
        categories = categories,
        selectedCategory = selectedCategory,
        onCategorySelected = { selectedCategory = it }
    )
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        label = { Text("Catatan") },
        placeholder = { Text("Contoh: Makan siang") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp)
    )
    val amountDisplay = formatRupiah(amountRaw)
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = amountDisplay,
        onValueChange = { amountRaw = it.filter { c -> c.isDigit() } },
        label = { Text("Nominal") },
        placeholder = { Text("0") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Text("Rp", color = Muted) },
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    Button(
        onClick = { 
            val amountValue = amountRaw.toLongOrNull() ?: 0L
            val catId = selectedCategory.toLongOrNull() ?: 0L
            onSave(amountValue, catId, note, selectedType == "Pengeluaran") 
        },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Green600),
        shape = RoundedCornerShape(13.dp),
        enabled = amountRaw.isNotBlank() && selectedCategory.isNotBlank()
    ) { Text("Simpan transaksi") }
}

@Composable
fun CategoryGrid(categories: List<CategoryUi>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        categories.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { category ->
                    CategoryCard(
                        category = category,
                        selected = category.id == selectedCategory,
                        onClick = { onCategorySelected(category.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun VoiceInputContent(
    voiceResult: VoiceResult?,
    isListening: Boolean,
    voiceError: String?,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onClearVoice: () -> Unit,
    categories: List<CategoryUi>,
    onSave: (Long, Long, String, Boolean) -> Unit
) {
    var confirmed by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(if (isListening) Pink50 else Green50)
                .border(2.dp, if (isListening) Pink400 else Green100, CircleShape)
                .clickable {
                    if (isListening) {
                        onStopVoice()
                    } else {
                        onClearVoice()
                        onStartVoice()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            SymbolBox(Icons.Filled.Mic, if (isListening) Pink400 else Color.White, if (isListening) Pink400 else Green600, 54.dp)
        }
        Spacer(Modifier.height(10.dp))
        if (isListening) {
            WaveBars()
            Spacer(Modifier.height(10.dp))
            Text("Mendengarkan...", color = Pink800, fontSize = 11.sp)
        } else if (voiceError != null) {
            Spacer(Modifier.height(10.dp))
            Text(voiceError, color = Danger, fontSize = 11.sp)
        } else {
            Spacer(Modifier.height(10.dp))
            Text("Coba bilang:", color = Muted, fontSize = 11.sp)
            Text("\"Beli makan siang dua puluh ribu\"", color = Green800, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        if (voiceResult != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Green50)
                    .padding(12.dp)
            ) {
                Text("HASIL DETEKSI", color = Green600, fontSize = 10.sp)
                ResultRow("Teks", voiceResult.rawText)
                ResultRow("Nominal", "Rp ${formatAmount(voiceResult.amount)}")
                ResultRow("Kategori", voiceResult.categoryName)
                ResultRow("Tipe", if (voiceResult.isExpense) "Pengeluaran" else "Pemasukan")
            }
            Button(
                onClick = {
                    if (!confirmed) {
                        confirmed = true
                        val cat = categories.find { it.name == voiceResult.categoryName }
                        val catId = cat?.id?.toLongOrNull() ?: 0L
                        onSave(voiceResult.amount, catId, voiceResult.rawText, voiceResult.isExpense)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green600),
                shape = RoundedCornerShape(13.dp),
                enabled = !confirmed
            ) { Text(if (confirmed) "Tersimpan!" else "Konfirmasi & simpan") }
            Text("Tidak akurat? Edit manual", color = Pink800, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp).clickable {
                onClearVoice()
            })
        }
    }
}

@Composable
fun InputModeChip(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Green50 else CardSoft)
            .border(1.dp, if (selected) Green100 else Color.Black.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Green800 else Color(0xFF666666),
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(label, color = if (selected) Green800 else Color(0xFF666666), fontSize = 12.sp)
    }
}

@Composable
fun CategoryCard(
    category: CategoryUi,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .then(modifier)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Green50 else CardSoft)
            .border(1.dp, if (selected) Green100 else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SymbolBox(iconForName(category.iconName), Green600, Green50, 30.dp)
        Text(
            category.name,
            color = if (selected) Green800 else Ink,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, fontSize = 11.sp)
        Text(value, color = Green800, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun WaveBars() {
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
