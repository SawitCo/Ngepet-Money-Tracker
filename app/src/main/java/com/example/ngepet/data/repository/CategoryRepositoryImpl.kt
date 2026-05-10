package com.example.ngepet.data.repository

import com.example.ngepet.data.local.dao.CategoryDao
import com.example.ngepet.data.local.entity.CategoryEntity
import com.example.ngepet.domain.model.Category
import com.example.ngepet.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category.toEntity())
    }
}

private fun CategoryEntity.toDomain(): Category = Category(
    id = id.toString(),
    name = name,
    iconName = iconName
)

private fun Category.toEntity(): CategoryEntity = CategoryEntity(
    name = name,
    iconName = iconName,
    isExpense = true
)
