package com.example.ngepet.data.repository

import com.example.ngepet.data.local.dao.TransactionDao
import com.example.ngepet.data.local.entity.TransactionEntity
import com.example.ngepet.domain.model.InputType
import com.example.ngepet.domain.model.Transaction
import com.example.ngepet.domain.model.TransactionType
import com.example.ngepet.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(id: String) {
        id.toLongOrNull()?.let { transactionDao.deleteTransactionById(it) }
    }
}

private fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id.toString(),
    type = if (isExpense) TransactionType.EXPENSE else TransactionType.INCOME,
    inputType = InputType.MANUAL,
    amount = amount.toDouble(),
    categoryId = categoryId.toString(),
    note = note.ifBlank { null },
    dateMillis = dateMillis
)

private fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    amount = amount.toLong(),
    categoryId = categoryId.toLongOrNull() ?: 0L,
    note = note ?: "",
    dateMillis = dateMillis,
    isExpense = type == TransactionType.EXPENSE
)
