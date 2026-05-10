package com.example.ngepet.domain.model

data class Budget(
    val id: String,
    val categoryId: String,
    val limit: Double,
    val month: Int,
    val year: Int
)
