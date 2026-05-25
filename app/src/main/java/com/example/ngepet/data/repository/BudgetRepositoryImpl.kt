package com.example.ngepet.data.repository

import com.example.ngepet.data.local.dao.BudgetDao
import com.example.ngepet.data.local.entity.BudgetEntity
import com.example.ngepet.domain.model.Budget
import com.example.ngepet.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getBudgets(month: Int, year: Int): Flow<List<Budget>> {
        return budgetDao.getBudgets(month, year).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertBudget(budget: Budget) {
        budgetDao.insertBudget(budget.toEntity())
    }

    override suspend fun deleteBudget(id: String) {
        id.toLongOrNull()?.let { budgetDao.deleteBudget(it) }
    }
}

private fun BudgetEntity.toDomain(): Budget = Budget(
    id = id.toString(),
    categoryId = categoryId.toString(),
    limit = limit.toDouble(),
    month = month,
    year = year
)

private fun Budget.toEntity(): BudgetEntity = BudgetEntity(
    categoryId = categoryId.toLongOrNull() ?: 0L,
    limit = limit.toLong(),
    month = month,
    year = year
)
