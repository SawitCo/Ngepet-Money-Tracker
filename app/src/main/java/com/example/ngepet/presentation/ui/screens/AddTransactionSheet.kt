package com.example.ngepet.presentation.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ngepet.data.source.SpeechToTransactionAdapter
import com.example.ngepet.domain.model.TransactionInputModel
import com.example.ngepet.presentation.ui.*
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
import com.example.ngepet.presentation.ui.theme.Pink50
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
    val catMap = remember(categories) {
        categories.associateBy { it.name }
    }

    var voicePreFill by remember { mutableStateOf<TransactionUi?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth().imePadding().navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            val title = if (editTxn != null) "Edit transaksi" else if (mode == InputSheetMode.Manual) "Tambah transaksi" else "Tambah via suara"
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text("x", modifier = Modifier.clickable { onClose() }.semantics { contentDescription = "Tutup" }, color = Muted)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InputModeChip("Manual", Icons.Filled.Keyboard, mode == InputSheetMode.Manual, Modifier.weight(1f)) { onModeChange(InputSheetMode.Manual) }
            InputModeChip("Suara", Icons.Filled.Mic, mode == InputSheetMode.Voice, Modifier.weight(1f)) { onModeChange(InputSheetMode.Voice) }
        }
        Spacer(Modifier.height(12.dp))
        AnimatedContent(targetState = mode, transitionSpec = {
            val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
            slideInHorizontally(tween(200)) { dir * it } togetherWith
                slideOutHorizontally(tween(200)) { -dir * it }
        }, label = "ModeSwitch") { currentMode ->
            if (currentMode == InputSheetMode.Manual) {
                ManualInputContent(
                    categories = categories,
                    editTxn = editTxn ?: voicePreFill,
                    onSave = { amount, catId, note, dateMillis, isExpense ->
                        voicePreFill = null
                        onSave(amount, catId, note, dateMillis, isExpense)
                    },
                    onUpdate = onUpdate
                )
            } else {
                voicePreFill = null
                VoiceInputContent(
                    categories = categories,
                    catMap = catMap,
                    onConfirm = { amount, catId, note, dateMillis, isExpense ->
                        onSave(amount, catId, note, dateMillis, isExpense)
                    },
                    onSwitchToManual = { parsed ->
                        val catId = parsed.categoryName?.let { name ->
                            catMap[name]?.id?.toLongOrNull()
                                ?: catMap["Lainnya"]?.id?.toLongOrNull()
                        } ?: 0L
                        voicePreFill = TransactionUi(
                            id = "",
                            amount = parsed.amount.toLong(),
                            categoryName = parsed.categoryName ?: "Lainnya",
                            categoryIcon = categories.find { it.id == catId.toString() }?.iconName ?: "MoreHoriz",
                            categoryId = catId.toString(),
                            note = parsed.note ?: "",
                            dateMillis = System.currentTimeMillis(),
                            isExpense = parsed.type != com.example.ngepet.domain.model.TransactionType.INCOME
                        )
                        onModeChange(InputSheetMode.Manual)
                    }
                )
            }
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
    val scrollState = rememberScrollState()
    var amountRaw by remember { mutableStateOf(if (isEditing) editTxn!!.amount.toString() else "") }
    var note by remember { mutableStateOf(editTxn?.note ?: "") }
    var selectedType by remember { mutableStateOf(if (isEditing && !editTxn!!.isExpense) "Pemasukan" else "Pengeluaran") }
    var selectedCategory by remember { mutableStateOf(editTxn?.categoryId ?: categories.firstOrNull()?.id ?: "") }
    val initialCal = remember { Calendar.getInstance().apply { editTxn?.let { timeInMillis = it.dateMillis } } }
    val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    var selectedDateMillis by remember { mutableStateOf(initialCal.timeInMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)) {

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
                if (isEditing && editTxn!!.id.isNotBlank() && onUpdate != null) {
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
fun VoiceInputContent(
    categories: List<CategoryUi>,
    catMap: Map<String, CategoryUi>,
    onConfirm: (Long, Long, String, Long, Boolean) -> Unit,
    onSwitchToManual: (TransactionInputModel) -> Unit
) {
    val context = LocalContext.current
    val adapter = remember { SpeechToTransactionAdapter() }

    var isListening by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<TransactionInputModel?>(null) }
    var rawText by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val sr = SpeechRecognizer.createSpeechRecognizer(context)
            recognizer = sr
            sr.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { isListening = true }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { isListening = false }
                override fun onError(error: Int) {
                    isListening = false
                    errorMsg = when (error) {
                        SpeechRecognizer.ERROR_NETWORK -> "Cek koneksi internet"
                        SpeechRecognizer.ERROR_AUDIO -> "Gagal mengakses mikrofon"
                        SpeechRecognizer.ERROR_NO_MATCH -> "Tidak terdeteksi, coba lagi"
                        else -> "Gagal mendeteksi suara"
                    }
                    recognizer?.destroy()
                    recognizer = null
                }
                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        rawText = matches[0]
                        result = adapter.adapt(matches[0])
                    }
                    recognizer?.destroy()
                    recognizer = null
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            sr.startListening(intent)
        } else {
            errorMsg = "Izin mikrofon diperlukan"
        }
    }

    DisposableEffect(Unit) {
        onDispose { recognizer?.destroy() }
    }

    val parsed = result

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (parsed == null) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape)
                    .background(if (isListening) Pink50 else Green50)
                    .border(2.dp, if (isListening) Pink400.copy(alpha = 0.4f) else Green100, CircleShape)
                    .clickable {
                        if (isListening) {
                            recognizer?.stopListening()
                            recognizer?.destroy()
                            recognizer = null
                            isListening = false
                        } else {
                            errorMsg = null
                            permLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                SymbolBox(Icons.Filled.Mic, Color.White, Green600, 54.dp)
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.height(90.dp), contentAlignment = Alignment.TopCenter) {
                if (isListening) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.height(26.dp), contentAlignment = Alignment.Center) {
                            WaveformBars()
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Dengarkan...", color = Pink400, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("Coba bilang:\n\"Beli makan siang dua puluh ribu\"", color = Muted, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 17.sp)
                    }
                } else {
                    Text("Tekan mikrofon untuk mulai", color = Muted, fontSize = 12.sp)
                }
            }
            if (errorMsg != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg!!, color = Color(0xFFA32D2D), fontSize = 11.sp)
            }
        } else {
            VoiceResultPreview(
                parsed = parsed,
                rawText = rawText,
                onConfirm = {
                    val catId = parsed.categoryName?.let { name ->
                        catMap[name]?.id?.toLongOrNull()
                            ?: catMap["Lainnya"]?.id?.toLongOrNull()
                    } ?: 0L
                    val isExpense = parsed.type != com.example.ngepet.domain.model.TransactionType.INCOME
                    onConfirm(parsed.amount.toLong(), catId, parsed.note ?: "", System.currentTimeMillis(), isExpense)
                },
                onRetry = {
                    result = null
                    rawText = null
                    errorMsg = null
                },
                onSwitchToManual = { onSwitchToManual(parsed) }
            )
        }
    }
}

@Composable
private fun WaveformBars() {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(7) { index ->
            val height by infiniteTransition.animateFloat(
                initialValue = 6f,
                targetValue = when (index % 3) { 0 -> 24f; 1 -> 14f; else -> 20f },
                animationSpec = infiniteRepeatable(
                    animation = tween(600 + index * 100, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "bar_$index"
            )
            Box(
                Modifier.size(width = 4.dp, height = height.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (index % 2 == 0) Pink400 else Green600)
            )
        }
    }
}

@Composable
private fun VoiceResultPreview(
    parsed: TransactionInputModel,
    rawText: String?,
    onConfirm: () -> Unit,
    onRetry: () -> Unit,
    onSwitchToManual: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(Green50),
            contentAlignment = Alignment.Center
        ) {
            SymbolBox(Icons.Filled.Mic, Green600, Green50, 44.dp)
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Green50).padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Hasil deteksi", color = Green800, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                if (!rawText.isNullOrBlank()) {
                    ResultRow("Terdeteksi", "\"$rawText\"")
                }
                ResultRow("Nominal", "Rp ${formatRupiah(parsed.amount.toLong().toString())}")
                ResultRow("Kategori", parsed.categoryName ?: "—")
                ResultRow("Tipe", if (parsed.type == com.example.ngepet.domain.model.TransactionType.INCOME) "Pemasukan" else "Pengeluaran")
                if (!parsed.note.isNullOrBlank()) {
                    ResultRow("Catatan", parsed.note)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Green600),
            shape = RoundedCornerShape(13.dp)
        ) { Text("Konfirmasi & simpan") }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text("Tidak akurat? ", color = Muted, fontSize = 11.sp)
            Text("Edit manual", color = Pink400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onSwitchToManual() })
        }
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text("atau ", color = Muted, fontSize = 11.sp)
            Text("Coba lagi", color = Green600, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onRetry() })
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, fontSize = 11.sp)
        Text(value, color = Green800, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
