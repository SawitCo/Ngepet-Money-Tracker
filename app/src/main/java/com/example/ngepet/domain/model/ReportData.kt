package com.example.ngepet.domain.model

data class ReportData(
    val period: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val categoryBreakdown: List<CategoryBreakdown>
)

data class CategoryBreakdown(
    val categoryName: String,
    val percentage: Double,
    val amount: Double,
    val colorHex: String
)
