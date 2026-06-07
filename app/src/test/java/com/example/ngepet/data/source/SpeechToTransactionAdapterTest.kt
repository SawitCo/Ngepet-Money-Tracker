package com.example.ngepet.data.source

import com.example.ngepet.domain.model.TransactionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SpeechToTransactionAdapterTest {

    private lateinit var adapter: SpeechToTransactionAdapter

    @Before
    fun setup() {
        adapter = SpeechToTransactionAdapter()
    }

    // --- parseAmount ---

    @Test
    fun `parseAmount - word-based ribu`() {
        val result = adapter.adapt("beli makan dua puluh ribu")
        assertEquals(20000.0, result.amount, 0.01)
    }

    @Test
    fun `parseAmount -Rp prefix with dot-separated number`() {
        val result = adapter.adapt("gajian Rp30.000")
        assertEquals(30000.0, result.amount, 0.01)
    }

    @Test
    fun `parseAmount - digit-only text`() {
        val result = adapter.adapt("beli baju 50000")
        assertEquals(50000.0, result.amount, 0.01)
    }

    @Test
    fun `parseAmount - seratus ribu`() {
        val result = adapter.adapt("seratus ribu")
        assertEquals(100000.0, result.amount, 0.01)
    }

    @Test
    fun `parseAmount - dua ratus lima puluh`() {
        val result = adapter.adapt("dua ratus lima puluh")
        assertEquals(250.0, result.amount, 0.01)
    }

    @Test
    fun `parseAmount - lima juta`() {
        val result = adapter.adapt("lima juta")
        assertEquals(5000000.0, result.amount, 0.01)
    }

    @Test
    fun `parseAmount - no number returns 0`() {
        val result = adapter.adapt("beli makan")
        assertEquals(0.0, result.amount, 0.01)
    }

    @Test
    fun `parseAmount -Rp30_000 without space`() {
        val result = adapter.adapt("Rp30.000")
        assertEquals(30000.0, result.amount, 0.01)
    }

    @Test
    fun `parseAmount - gajian Rp30_000 full phrase`() {
        val result = adapter.adapt("gajian Rp30.000")
        assertEquals(30000.0, result.amount, 0.01)
    }

    // --- detectType ---

    @Test
    fun `detectType - gajian is INCOME`() {
        val result = adapter.adapt("gajian dua puluh ribu")
        assertEquals(TransactionType.INCOME, result.type)
    }

    @Test
    fun `detectType - beli is EXPENSE`() {
        val result = adapter.adapt("beli makan dua puluh ribu")
        assertEquals(TransactionType.EXPENSE, result.type)
    }

    @Test
    fun `detectType - bayar is EXPENSE`() {
        val result = adapter.adapt("bayar listrik seratus ribu")
        assertEquals(TransactionType.EXPENSE, result.type)
    }

    @Test
    fun `detectType - terima is INCOME`() {
        val result = adapter.adapt("terima transfer lima ratus ribu")
        assertEquals(TransactionType.INCOME, result.type)
    }

    // --- detectCategory ---

    @Test
    fun `detectCategory - makan maps to Makanan`() {
        val result = adapter.adapt("beli makan siang")
        assertEquals("Makanan", result.categoryName)
    }

    @Test
    fun `detectCategory - gaji maps to Pekerjaan`() {
        val result = adapter.adapt("gaji lima juta")
        assertEquals("Pekerjaan", result.categoryName)
    }

    @Test
    fun `detectCategory - gojek maps to Transport`() {
        val result = adapter.adapt("bayar gojek dua puluh ribu")
        assertEquals("Transport", result.categoryName)
    }

    @Test
    fun `detectCategory - listrik maps to Tagihan`() {
        val result = adapter.adapt("bayar listrik seratus ribu")
        assertEquals("Tagihan", result.categoryName)
    }

    @Test
    fun `detectCategory - obat maps to Kesehatan when no other keyword`() {
        val result = adapter.adapt("bayar obat tiga puluh ribu")
        assertEquals("Kesehatan", result.categoryName)
    }

    @Test
    fun `detectCategory - beli without known keyword returns Belanja`() {
        val result = adapter.adapt("beli sesuatu dua puluh ribu")
        assertEquals("Belanja", result.categoryName)
    }

    // --- cleanNote ---

    @Test
    fun `cleanNote - strips action words and multipliers`() {
        val result = adapter.adapt("beli makan dua puluh ribu")
        // "beli" and "makan" are action words, "puluh" and "ribu" are multipliers
        // "dua" is a number word that is NOT in the stripped lists
        assertFalse(result.note?.contains("beli") == true)
        assertFalse(result.note?.contains("makan") == true)
        assertFalse(result.note?.contains("puluh") == true)
        assertFalse(result.note?.contains("ribu") == true)
    }

    @Test
    fun `cleanNote - strips Rp`() {
        val result = adapter.adapt("beli makan Rp30.000")
        assertFalse(result.note?.contains("rp") == true)
        assertFalse(result.note?.contains("Rp") == true)
    }

    @Test
    fun `cleanNote - returns null if blank`() {
        val result = adapter.adapt("20000")
        assertNull(result.note)
    }
}
