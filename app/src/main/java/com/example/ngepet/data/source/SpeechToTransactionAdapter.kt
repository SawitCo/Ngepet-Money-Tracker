package com.example.ngepet.data.source

import com.example.ngepet.domain.model.InputType
import com.example.ngepet.domain.model.TransactionInputModel
import com.example.ngepet.domain.model.TransactionType

class SpeechToTransactionAdapter {

    fun adapt(rawSpeech: String): TransactionInputModel {
        val lower = rawSpeech.lowercase().trim()
        val amount = parseAmount(lower)
        val type = detectType(lower)
        val category = detectCategory(lower)
        val note = cleanNote(lower, amount, type, category)

        return TransactionInputModel(
            amount = amount,
            categoryName = category,
            type = type,
            note = note,
            confidence = if (amount > 0) 0.8f else 0.3f
        )
    }

    private fun parseAmount(text: String): Double {
        val digitPattern = Regex("""(\d[\d.]*)\s*(?:ribu|ratus|puluh)?""")
        val numberWords = mapOf(
            "nol" to 0, "satu" to 1, "dua" to 2, "tiga" to 3, "empat" to 4,
            "lima" to 5, "enam" to 6, "tujuh" to 7, "delapan" to 8, "sembilan" to 9,
            "sepuluh" to 10, "sebelas" to 11, "seratus" to 100, "seribu" to 1000
        )

        var total = 0.0
        var current = 0

        text.split(" ").forEach { word ->
            when (word) {
                "ribu" -> { total += current * 1000; current = 0 }
                "ratus" -> { total += current * 100; current = 0 }
                "puluh" -> { total += current * 10; current = 0 }
                "belas" -> { current += 10 }
                "juta" -> { total += current * 1_000_000; current = 0 }
                else -> {
                    val num = numberWords[word] ?: word.toIntOrNull()
                    if (num != null) current = num
                }
            }
        }
        total += current

        val digitMatch = digitPattern.find(text)?.groupValues?.get(1)
        val digitAmount = digitMatch?.replace(".", "")?.toDoubleOrNull() ?: 0.0

        return maxOf(total, digitAmount)
    }

    private fun detectType(text: String): TransactionType {
        val expenseWords = listOf("beli", "bayar", "makan", "ongkos", "cicil", "top up")
        val incomeWords = listOf("terima", "gajian", "transfer masuk", "dapat", "bonus")
        return when {
            incomeWords.any { text.contains(it) } -> TransactionType.INCOME
            expenseWords.any { text.contains(it) } -> TransactionType.EXPENSE
            else -> TransactionType.EXPENSE
        }
    }

    private fun detectCategory(text: String): String? {
        val categoryMap = listOf(
            "makan" to "Makanan", "nasi" to "Makanan", "kantin" to "Makanan",
            "bensin" to "Transport", "naik" to "Transport", "gojek" to "Transport", "grab" to "Transport",
            "belanja" to "Belanja", "baju" to "Belanja",
            "nonton" to "Hiburan", "film" to "Hiburan", "game" to "Hiburan",
            "listrik" to "Tagihan", "air" to "Tagihan", "pulsa" to "Tagihan", "wifi" to "Tagihan",
            "obat" to "Kesehatan", "dokter" to "Kesehatan"
        )
        return categoryMap.firstOrNull { text.contains(it.first) }?.second
    }

    private fun cleanNote(text: String, amount: Double, type: TransactionType?, category: String?): String? {
        var cleaned = text
        cleaned = cleaned.replace(Regex("""\d[\d.]*"""), "").trim()
        cleaned = cleaned.replace(Regex("""\b(ribu|ratus|puluh|belas|juta)\b"""), "").trim()
        category?.let { cleaned = cleaned.replace(it.lowercase(), "").trim() }
        return cleaned.ifBlank { null }
    }
}
