package com.example.ngepet.domain.factory

import com.example.ngepet.domain.model.Category
import com.example.ngepet.domain.model.InputType
import com.example.ngepet.domain.model.Transaction
import com.example.ngepet.domain.model.TransactionType

interface TransactionComponentFactory {
    fun createTransaction(
        amount: Double, categoryId: String, note: String?, dateMillis: Long
    ): Transaction
    fun createDefaultCategory(): Category
    val inputType: InputType
}

class ManualInputFactory : TransactionComponentFactory {
    override val inputType: InputType = InputType.MANUAL
    override fun createTransaction(amount: Double, categoryId: String, note: String?, dateMillis: Long): Transaction =
        Transaction("0", TransactionType.EXPENSE, inputType, amount, categoryId, note, dateMillis)
    override fun createDefaultCategory(): Category = Category("general", "Umum", "Receipt")
}

class VoiceInputFactory : TransactionComponentFactory {
    override val inputType: InputType = InputType.VOICE
    override fun createTransaction(amount: Double, categoryId: String, note: String?, dateMillis: Long): Transaction =
        Transaction("0", TransactionType.EXPENSE, inputType, amount, categoryId, note, dateMillis)
    override fun createDefaultCategory(): Category = Category("uncategorized", "Belum dikategorikan", "Help")
}
