package com.mpcs51205.itemservice.service

import com.mpcs51205.itemservice.models.Category
import com.mpcs51205.itemservice.models.Item
import com.mpcs51205.itemservice.repository.CategoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.lang.Exception
import java.util.*

@Service
class CategoryService(val categoryRepository: CategoryRepository) {

    fun getCategoryById(catId: UUID): Category {
        return categoryRepository.findByIdOrNull(catId) ?: throw Exception("Category does not exist in database.")
    }

    fun createCategory(category: Category): Category {
        saveCategory(category)
        println("CREATED CATEGORY: ${category.categoryDescription}")
        return category
    }

    fun saveCategory(category: Category) {
        try {
            categoryRepository.save(category)
            println("SAVED CATEGORY")
        } catch (e: Exception) {
            throw Exception("ERROR SAVING CATEGORY: ${e.message}")
        }
    }

    fun deleteCategory(catId: UUID) {
        try {
            categoryRepository.delete(getCategoryById(catId))
            println("DELETED CATEGORY.")
        } catch (e: Exception) {
            throw Exception("Problem deleting category from database.")
        }
    }

    fun modifyCategory(catToModify: UUID, newDescription: String): Category {
        val toModify: Category = getCategoryById(catToModify)
        try {
            toModify.categoryDescription = newDescription
            saveCategory(toModify)
            println("Successfully modified category to '${newDescription}'")
            return toModify
        } catch (e: Exception){
            throw Exception("Cannot modify category ${catToModify}.")
        }
    }

    fun getAll(ids: Collection<UUID>?) = (ids?.map { categoryRepository.findByIdOrNull(it) }?.filterNotNull()
        ?: categoryRepository.findAll())

    fun getItemsByCategory(catName: String) = categoryRepository.getItemsByCategory(catName) ?:
    throw Exception("NO MATCHING ITEMS IN THAT CATEGORY")
}