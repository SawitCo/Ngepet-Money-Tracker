package com.example.ngepet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Long,
    val categoryId: Long,
    val note: String,
    val dateMillis: Long,
    val isExpense: Boolean
)
