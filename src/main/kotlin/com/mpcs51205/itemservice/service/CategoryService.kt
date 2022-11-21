package com.mpcs51205.itemservice.service

import com.mpcs51205.itemservice.models.ItemCategory
import com.mpcs51205.itemservice.repository.CategoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.lang.Exception
import java.util.*

@Service
class CategoryService(val categoryRepository: CategoryRepository) {

    fun getCategoryById(catId: UUID): ItemCategory {
        return categoryRepository.findByIdOrNull(catId) ?: throw Exception("No such category.")
    }

    fun createCategory(category: ItemCategory): ItemCategory {
        saveCategory(category)
        return category
    }

    fun saveCategory(category: ItemCategory) {
        try {
            categoryRepository.save(category)
        } catch (e: Exception) {
            throw e
        }
    }

    fun deleteCategory(catId: UUID) = categoryRepository.delete(getCategoryById(catId))

    fun modifyCategory(catToModify: UUID, newDescription: String): ItemCategory {
        val toModify: ItemCategory = getCategoryById(catToModify)
        try {
            toModify.categoryDescription = newDescription
            saveCategory(toModify)
            return toModify
        } catch (e: Exception){
            throw Exception("Cannot modify category ${catToModify}.")
        }
    }
}