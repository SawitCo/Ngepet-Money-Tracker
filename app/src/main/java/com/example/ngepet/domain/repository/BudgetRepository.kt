package com.example.ngepet.domain.repository

import com.example.ngepet.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgets(month: Int, year: Int): Flow<List<Budget>>
    suspend fun upsertBudget(budget: Budget)
    suspend fun deleteBudget(id: String)
}
