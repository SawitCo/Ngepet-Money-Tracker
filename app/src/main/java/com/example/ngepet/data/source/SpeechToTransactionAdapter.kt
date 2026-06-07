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
        val normalized = text
            .replace(Regex("""\b[Rr][Pp]\.?\s?"""), "")
            .replace(Regex("""\brupiah\s?"""), "")

        val hasRibuOrJuta = Regex("""\b(ribu|juta)\b""").containsMatchIn(normalized)

        val currencyNum = Regex("""\d{1,3}(?:\.\d{3})+""").find(normalized)?.value
        if (currencyNum != null) {
            return currencyNum.replace(".", "").toDoubleOrNull() ?: 0.0
        }

        val expanded = normalized
            .replace(Regex("""\bseperempat\b"""), "0.25")
            .replace(Regex("""\bsetengah\b"""), "0.5")
            .replace(Regex("""\bseratus\b"""), "satu ratus")
            .replace(Regex("""\bseribu\b"""), "satu ribu")
            .replace(Regex("""\bsejuta\b"""), "satu juta")
            .replace(Regex("""\bsebelas\b"""), "satu belas")
            .replace(Regex("""\bsepuluh\b"""), "satu puluh")

        val ones = mapOf(
            "nol" to 0, "satu" to 1, "dua" to 2, "tiga" to 3, "empat" to 4,
            "lima" to 5, "enam" to 6, "tujuh" to 7, "delapan" to 8, "sembilan" to 9
        )

        var total = 0.0
        var block = 0L
        var current = 0L

        for (word in expanded.split(" ")) {
            when {
                ones.containsKey(word) -> {
                    block += current
                    current = ones[word]!!.toLong()
                }
                word == "belas" -> {
                    current = (current.coerceAtLeast(1)) + 10
                }
                word == "puluh" -> {
                    current = (current.coerceAtLeast(1)) * 10
                }
                word == "ratus" -> {
                    current = (current.coerceAtLeast(1)) * 100
                }
                word == "ribu" -> {
                    block += current
                    if (block == 0L) block = 1
                    total += block * 1_000
                    block = 0; current = 0
                }
                word == "juta" -> {
                    block += current
                    if (block == 0L) block = 1
                    total += block * 1_000_000
                    block = 0; current = 0
                }
                word.toIntOrNull() != null -> {
                    val n = word.toLong()
                    block += current
                    current = n
                }
            }
        }
        block += current
        total += block

        if (total == 0.0) {
            val digits = Regex("""\d+""").findAll(normalized).map { it.value.toLong() }.toList()
            if (digits.isNotEmpty()) return digits.first().toDouble()
        }
        return total
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
            "skincare" to "Belanja", "makeup" to "Belanja", "alat tulis" to "Belanja", "beli" to "Belanja",
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
        val cleaned = text
            .replace(Regex("""[Rr][Pp]\.?\s?"""), "")
            .replace(Regex("""\d+(\.\d+)?"""), "")
            .replace(Regex("""\b(ribu|ratus|puluh|belas|juta|sejuta|seribu|seratus|seperempat|setengah)\b"""), "")
            .replace(Regex("""\b(beli|bayar|makan|ongkos|belanja|jajan|tarik|isi|terima|setor|transfer|dapet|dapat)\b"""), "")
            .replace(Regex("""\b(di|ke|saya|aku|sudah|telah|untuk|dan|sama|dengan|pakai|pake)\b"""), "")
            .trim()
            .replace(Regex("""\s+"""), " ")
        return cleaned.ifBlank { null }
    }
}
