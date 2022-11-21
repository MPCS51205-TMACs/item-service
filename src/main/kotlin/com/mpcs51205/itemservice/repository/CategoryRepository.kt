package com.mpcs51205.itemservice.repository

import com.mpcs51205.itemservice.models.Item
import com.mpcs51205.itemservice.models.ItemCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CategoryRepository: JpaRepository <ItemCategory, UUID> {
    fun getCatById(id: UUID): ItemCategory?
    fun findByCategoryDescriptionIs(description: String): ItemCategory?
}