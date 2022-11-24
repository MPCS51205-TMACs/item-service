package com.mpcs51205.itemservice.repository

import com.mpcs51205.itemservice.models.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CategoryRepository: JpaRepository <Category, UUID> {
    fun getCatById(id: UUID): Category?
    fun findByCategoryDescriptionIs(description: String): Category?
}