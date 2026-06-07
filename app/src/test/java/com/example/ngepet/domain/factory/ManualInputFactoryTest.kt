package com.example.ngepet.domain.factory

import com.example.ngepet.domain.model.InputType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ManualInputFactoryTest {

    private lateinit var factory: ManualInputFactory

    @Before
    fun setup() {
        factory = ManualInputFactory()
    }

    @Test
    fun `inputType is MANUAL`() {
        assertEquals(InputType.MANUAL, factory.inputType)
    }

    @Test
    fun `createTransaction - returns transaction with MANUAL inputType`() {
        val tx = factory.createTransaction(
            amount = 50000.0,
            categoryId = "1",
            note = "Makan siang",
            dateMillis = System.currentTimeMillis()
        )
        assertEquals(InputType.MANUAL, tx.inputType)
        assertEquals(50000.0, tx.amount, 0.01)
        assertEquals("1", tx.categoryId)
        assertEquals("Makan siang", tx.note)
    }

    @Test
    fun `createDefaultCategory - returns Umum with Receipt icon`() {
        val cat = factory.createDefaultCategory()
        assertEquals("Umum", cat.name)
        assertEquals("Receipt", cat.iconName)
        assertEquals("general", cat.id)
    }
}
