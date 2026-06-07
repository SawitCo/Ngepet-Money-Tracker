package com.example.ngepet.data.repository

import com.example.ngepet.data.local.dao.CategoryDao
import com.example.ngepet.data.local.entity.CategoryEntity
import com.example.ngepet.domain.model.Category
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CategoryRepositoryImplTest {

    private lateinit var dao: CategoryDao
    private lateinit var repo: CategoryRepositoryImpl

    @Before
    fun setup() {
        dao = mockk()
        repo = CategoryRepositoryImpl(dao)
    }

    @Test
    fun `insertCategory calls dao with correct entity`() = runTest {
        val category = Category("0", "Makanan", "Restaurant")
        coEvery { dao.insertCategory(any()) } returns 1L

        repo.insertCategory(category)

        coVerify(exactly = 1) { dao.insertCategory(match {
            it.name == "Makanan" && it.iconName == "Restaurant"
        })}
    }

    @Test
    fun `getAllCategories maps entities to domain models`() = runTest {
        val entities = listOf(
            CategoryEntity(id = 1, name = "Makanan", iconName = "Restaurant", isExpense = true),
            CategoryEntity(id = 2, name = "Transport", iconName = "Commute", isExpense = true),
            CategoryEntity(id = 3, name = "Pekerjaan", iconName = "Payments", isExpense = true)
        )
        every { dao.getAllCategories() } returns flowOf(entities)

        val result = repo.getAllCategories().first()

        assertEquals(3, result.size)
        assertEquals("Makanan", result[0].name)
        assertEquals("Restaurant", result[0].iconName)
        assertEquals("1", result[0].id)
        assertEquals("Transport", result[1].name)
        assertEquals("Pekerjaan", result[2].name)
    }
}
