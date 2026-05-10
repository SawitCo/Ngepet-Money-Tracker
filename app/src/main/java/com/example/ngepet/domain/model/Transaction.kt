package com.example.ngepet.domain.model

data class Transaction(
    val id: String,
    val type: TransactionType,
    val inputType: InputType,
    val amount: Double,
    val categoryId: String,
    val note: String?,
    val dateMillis: Long
)
