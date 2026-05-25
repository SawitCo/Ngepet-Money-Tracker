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
        val note = cleanNote(lower)

        return TransactionInputModel(
            amount = amount,
            categoryName = category,
            type = type,
            note = note,
            confidence = if (amount > 0) 0.8f else 0.3f
        )
    }

    private fun parseAmount(text: String): Double {
        val numberWords = mapOf(
            "nol" to 0, "satu" to 1, "dua" to 2, "tiga" to 3, "empat" to 4,
            "lima" to 5, "enam" to 6, "tujuh" to 7, "delapan" to 8, "sembilan" to 9,
            "sepuluh" to 10, "sebelas" to 11, "seratus" to 100, "seribu" to 1000
        )
        var total = 0.0; var current = 0
        text.split(" ").forEach { word ->
            when (word) {
                "ribu" -> { total += current * 1000; current = 0 }
                "ratus" -> { total += current * 100; current = 0 }
                "puluh" -> { total += current * 10; current = 0 }
                "juta" -> { total += current * 1_000_000; current = 0 }
                else -> { val num = numberWords[word] ?: word.toIntOrNull(); if (num != null) current = num }
            }
        }
        val digits = Regex("""\d+""").findAll(text).map { it.value.toLong() }.toList()
        return maxOf(total + current, digits.firstOrNull()?.toDouble() ?: 0.0)
    }

    private fun detectType(text: String): TransactionType {
        val expenseWords = listOf("beli", "bayar", "makan", "ongkos")
        val incomeWords = listOf("terima", "gajian", "transfer masuk", "bonus")
        return when {
            incomeWords.any { text.contains(it) } -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }
    }

    private fun detectCategory(text: String): String? {
        val map = listOf(
            "makan" to "Makanan", "nasi" to "Makanan",
            "bensin" to "Transport", "gojek" to "Transport", "grab" to "Transport",
            "belanja" to "Belanja", "baju" to "Belanja",
            "nonton" to "Hiburan", "film" to "Hiburan", "game" to "Hiburan",
            "listrik" to "Tagihan", "pulsa" to "Tagihan",
            "obat" to "Kesehatan", "dokter" to "Kesehatan"
        )
        return map.firstOrNull { text.contains(it.first) }?.second
    }

    private fun cleanNote(text: String): String? {
        val cleaned = text.replace(Regex("""\d+"""), "").trim()
            .replace(Regex("""\b(ribu|ratus|puluh|belas|juta)\b"""), "").trim()
            .replace(Regex("""\b(beli|bayar|makan|ongkos)\b"""), "").trim()
        return cleaned.ifBlank { null }
    }
}
