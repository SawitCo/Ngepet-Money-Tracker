package com.example.ngepet.presentation.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class TransactionUi(
    val id: String,
    val amount: Long,
    val categoryName: String,
    val categoryIcon: String,
    val note: String,
    val dateMillis: Long,
    val isExpense: Boolean,
    val source: String = "Manual"
)

data class CategoryUi(
    val id: String,
    val name: String,
    val iconName: String
)

data class BudgetUi(
    val category: String,
    val period: String,
    val used: String,
    val limit: String,
    val progress: Float,
    val status: String,
    val iconName: String = "MoreHoriz",
    val spentAmount: Long = 0,
    val limitAmount: Long = 0,
    val overAmount: Long = 0
)

data class OnboardingPageUi(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val bg: Color
)
