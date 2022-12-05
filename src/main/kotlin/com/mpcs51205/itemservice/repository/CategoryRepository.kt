package com.mpcs51205.itemservice.repository

import com.mpcs51205.itemservice.models.Category
import com.mpcs51205.itemservice.models.Item
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CategoryRepository: JpaRepository <Category, UUID> {
    fun findByCategoryDescriptionIs(description: String): Category?
    @Query("SELECT DISTINCT c.items from Category c WHERE c.categoryDescription = ?1")
    fun getItemsByCategory(description: String): Collection<Item>?
}