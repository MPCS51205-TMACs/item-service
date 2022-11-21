package com.mpcs51205.itemservice.repository

import com.mpcs51205.itemservice.models.Item
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ItemRepository: JpaRepository <Item, UUID> {
    fun getItemById(id: UUID): Item?
}