package com.example.ngepet.domain.model

data class TransactionInputModel(
    val amount: Double,
    val categoryName: String?,
    val type: TransactionType?,
    val note: String?,
    val confidence: Float
)
