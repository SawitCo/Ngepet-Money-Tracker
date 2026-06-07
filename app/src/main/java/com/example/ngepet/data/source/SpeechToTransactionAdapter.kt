package com.example.ngepet.data.source

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
            "sepuluh" to 10, "sebelas" to 11, "seratus" to 100, "seribu" to 1000,
            "sejuta" to 1_000_000
        )
        val multipliers = mapOf(
            "ribu" to 1_000, "ratus" to 100, "puluh" to 10, "belas" to 11, "juta" to 1_000_000
        )

        var total = 0.0
        var current = 0
        val words = text.split(" ")

        var i = 0
        while (i < words.size) {
            val word = words[i]
            when {
                word == "se" && i + 1 < words.size -> {
                    val next = words[i + 1]
                    multiplier(next)?.let { mul ->
                        total += 1.0 * mul
                        current = 0
                        i += 2
                        continue
                    }
                    current = 1; i++; continue
                }
                word == "seperempat" -> { total += 0.25; current = 0 }
                word == "setengah" -> { total += 0.5; current = 0 }
                multipliers.containsKey(word) -> {
                    val mul = multipliers[word] ?: 0
                    total += current.coerceAtLeast(1) * mul
                    current = 0
                }
                word == "setiap" || word == "per" -> { /* skip */ }
                else -> {
                    if (word.startsWith("se") && word.length > 2) {
                        val root = word.substring(2)
                        multiplier(root)?.let { mul ->
                            total += 1.0 * mul
                            current = 0
                        } ?: run {
                            val num = numberWords[word] ?: word.toIntOrNull()
                            if (num != null) current = num
                        }
                    } else {
                        val num = numberWords[word] ?: word.toIntOrNull()
                        if (num != null) current = num
                    }
                }
            }
            i++
        }
        total += current

        val digits = Regex("""\d+""").findAll(text).map { it.value.toLong() }.toList()
        return maxOf(total, digits.firstOrNull()?.toDouble() ?: 0.0)
    }

    private fun multiplier(word: String): Int? = when (word) {
        "ribu" -> 1_000; "ratus" -> 100; "puluh" -> 10; "belas" -> 11; "juta" -> 1_000_000
        else -> null
    }

    private fun detectType(text: String): TransactionType {
        val expenseWords = listOf(
            "beli", "bayar", "makan", "ongkos", "belanja", "jajan", "tarik",
            "transfer keluar", "bayarin", "bayar sekolah", "bayar kuliah",
            "isi bensin", "isi pulsa", "bayar listrik", "bayar air"
        )
        val incomeWords = listOf(
            "terima", "gajian", "transfer masuk", "bonus", "setor", "pemasukan",
            "upah", "kembalian", "gaji", "honor", "dapet", "dapat", "masuk",
            "kiriman", "rejeki", "hasil"
        )
        return when {
            incomeWords.any { text.contains(it) } -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }
    }

    private fun detectCategory(text: String): String? {
        val map = listOf(
            "makan" to "Makanan", "nasi" to "Makanan", "kopi" to "Makanan",
            "minum" to "Makanan", "sarapan" to "Makanan", "siang" to "Makanan",
            "jajan" to "Makanan", "cafe" to "Makanan", "restoran" to "Makanan",
            "bensin" to "Transport", "gojek" to "Transport", "grab" to "Transport",
            "ojek" to "Transport", "angkot" to "Transport", "bus" to "Transport",
            "transit" to "Transport", "bahan bakar" to "Transport", "tol" to "Transport",
            "belanja" to "Belanja", "baju" to "Belanja", "sepatu" to "Belanja",
            "skincare" to "Belanja", "makeup" to "Belanja", "alat tulis" to "Belanja",
            "nonton" to "Hiburan", "film" to "Hiburan", "game" to "Hiburan",
            "streaming" to "Hiburan", "netflix" to "Hiburan", "spotify" to "Hiburan",
            "listrik" to "Tagihan", "pulsa" to "Tagihan", "air" to "Tagihan",
            "internet" to "Tagihan", "wifi" to "Tagihan", "bpjs" to "Tagihan",
            "tagihan" to "Tagihan", "token" to "Tagihan",
            "obat" to "Kesehatan", "dokter" to "Kesehatan", "rumah sakit" to "Kesehatan",
            "klinik" to "Kesehatan", "vitamin" to "Kesehatan",
            "gaji" to "Pekerjaan", "honor" to "Pekerjaan", "kerja" to "Pekerjaan",
            "freelance" to "Pekerjaan"
        )
        return map.firstOrNull { text.contains(it.first) }?.second
    }

    private fun cleanNote(text: String): String? {
        val numberPattern = Regex("""\d+(\.\d+)?""")
        val multiplierPattern = Regex("""\b(ribu|ratus|puluh|belas|juta|sejuta|seribu|seratus|seperempat|setengah)\b""")
        val actionWords = Regex("""\b(beli|bayar|makan|ongkos|belanja|jajan|tarik|isi|terima|setor|transfer|dapet|dapat)\b""")
        val fillerPattern = Regex("""\b(di|ke|saya|aku|sudah|telah|untuk|dan|sama|dengan|pakai|pake)\b""")

        val cleaned = text
            .replace(numberPattern, "")
            .replace(multiplierPattern, "")
            .replace(actionWords, "")
            .replace(fillerPattern, "")
            .trim()
            .replace(Regex("""\s+"""), " ")
        return cleaned.ifBlank { null }
    }
}
