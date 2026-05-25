package com.example.ngepet.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

data class VoiceResult(
    val rawText: String,
    val amount: Long,
    val categoryName: String,
    val isExpense: Boolean
)

class VoiceRecognitionHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    var onResult: ((VoiceResult) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onListeningChanged: ((Boolean) -> Unit)? = null

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            onListeningChanged?.invoke(true)
        }

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            onListeningChanged?.invoke(false)
        }

        override fun onError(error: Int) {
            onListeningChanged?.invoke(false)
            val msg = when (error) {
                SpeechRecognizer.ERROR_NETWORK -> "Cek koneksi internet"
                SpeechRecognizer.ERROR_AUDIO -> "Gagal mengakses mikrofon"
                SpeechRecognizer.ERROR_NO_MATCH -> "Tidak terdeteksi, coba lagi"
                else -> "Gagal mendeteksi suara"
            }
            onError?.invoke(msg)
        }

        override fun onResults(results: Bundle?) {
            onListeningChanged?.invoke(false)
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                val parsed = parseVoiceInput(text)
                onResult?.invoke(parsed)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun startListening() {
        stopListening()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(listener)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun parseVoiceInput(text: String): VoiceResult {
        val lower = text.lowercase()
        val isExpense = !lower.contains("masuk") && !lower.contains("gaji") && !lower.contains("pemasukan")
        val categoryName = guessCategory(lower)
        val amount = extractAmount(lower)
        return VoiceResult(
            rawText = text,
            amount = amount,
            categoryName = categoryName,
            isExpense = isExpense
        )
    }

    private fun guessCategory(text: String): String {
        return when {
            text.contains("makan") || text.contains("nasi") || text.contains("siang") || text.contains("minum") -> "Makanan"
            text.contains("transport") || text.contains("bensin") || text.contains("angkot") || text.contains("grab") || text.contains("gojek") -> "Transport"
            text.contains("belanja") || text.contains("baju") || text.contains("sepatu") -> "Belanja"
            text.contains("hiburan") || text.contains("nonton") || text.contains("film") || text.contains("game") -> "Hiburan"
            text.contains("tagihan") || text.contains("listrik") || text.contains("air") || text.contains("pulsa") -> "Tagihan"
            text.contains("sehat") || text.contains("obat") || text.contains("dokter") -> "Kesehatan"
            text.contains("gaji") || text.contains("honor") -> "Gaji"
            else -> "Lainnya"
        }
    }

    private fun extractAmount(text: String): Long {
        val numberWords = mapOf(
            "nol" to 0, "satu" to 1, "dua" to 2, "tiga" to 3, "empat" to 4,
            "lima" to 5, "enam" to 6, "tujuh" to 7, "delapan" to 8, "sembilan" to 9,
            "sepuluh" to 10, "sebelas" to 11, "seratus" to 100, "seribu" to 1000,
            "ratus" to 100, "ribu" to 1000, "puluh" to 10, "belas" to 11
        )

        val digitPattern = Regex("\\d+")
        val digits = digitPattern.findAll(text).map { it.value.toLong() }.toList()
        if (digits.isNotEmpty()) return digits.first()

        val words = text.split(" ").map { it.lowercase() }
        var total = 0L
        var current = 0L
        for (word in words) {
            when {
                word in listOf("sejuta", "satu", "satu") && current == 0L -> current = 1
                word == "sejuta" || word == "juta" -> { total += current * 1_000_000; current = 0 }
                word == "seribu" || word == "ribu" -> { total += (current.coerceAtLeast(1)) * 1_000; current = 0 }
                word == "seratus" || word == "ratus" -> { total += (current.coerceAtLeast(1)) * 100; current = 0 }
                word == "belas" -> current += 10
                word == "puluh" -> current *= 10
                numberWords.containsKey(word) -> current += numberWords[word] ?: 0
            }
        }
        total += current
        return total * 1000
    }
}
