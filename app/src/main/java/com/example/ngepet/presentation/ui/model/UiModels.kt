package com.example.ngepet.presentation.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class TransactionUi(
    val title: String,
    val category: String,
    val amount: String,
    val isIncome: Boolean,
    val source: String,
    val icon: ImageVector,
    val color: Color,
    val bg: Color
)

data class CategoryUi(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val bg: Color
)

data class BudgetUi(
    val category: String,
    val period: String,
    val used: String,
    val limit: String,
    val progress: Float,
    val status: String,
    val icon: ImageVector,
    val color: Color,
    val bg: Color
)

data class OnboardingPageUi(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val bg: Color
)
