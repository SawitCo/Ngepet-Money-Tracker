package com.example.ngepet.domain.factory

import com.example.ngepet.domain.model.InputType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class VoiceInputFactoryTest {

    private lateinit var factory: VoiceInputFactory

    @Before
    fun setup() {
        factory = VoiceInputFactory()
    }

    @Test
    fun `inputType is VOICE`() {
        assertEquals(InputType.VOICE, factory.inputType)
    }

    @Test
    fun `createTransaction - returns transaction with VOICE inputType`() {
        val tx = factory.createTransaction(
            amount = 20000.0,
            categoryId = "2",
            note = "Beli kopi",
            dateMillis = System.currentTimeMillis()
        )
        assertEquals(InputType.VOICE, tx.inputType)
        assertEquals(20000.0, tx.amount, 0.01)
        assertEquals("2", tx.categoryId)
    }

    @Test
    fun `createDefaultCategory - returns Belum dikategorikan with Help icon`() {
        val cat = factory.createDefaultCategory()
        assertEquals("Belum dikategorikan", cat.name)
        assertEquals("Help", cat.iconName)
        assertEquals("uncategorized", cat.id)
    }
}
