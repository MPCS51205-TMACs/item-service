package com.mpcs51205.itemservice.repository

import com.mpcs51205.itemservice.models.Bookmark
import com.mpcs51205.itemservice.models.Item
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ItemRepository: JpaRepository <Item, UUID> {
    fun getItemById(id: UUID): Item?
    fun findByDescriptionContainingIgnoreCase(query: String): Collection<Item>?
    @Query("SELECT DISTINCT b.item from Bookmark b WHERE b.userId = ?1")
    fun getItemsByBookmarks(userId: UUID): Collection<Item>?

    @Query("SELECT DISTINCT b.userId FROM Bookmark b WHERE b.item.id = ?1")
    fun getUsersByBookmarkedItem(itemId: UUID): Collection<UUID>?

    @Query("SELECT DISTINCT i.id FROM Item i WHERE i.userId = ?1")
    fun getItemsByUser(userId: UUID): Collection<UUID>?

    @Query("SELECT DISTINCT i FROM Item i WHERE i.counterfeit = true OR i.inappropriate = true")
    fun getMarkedItems(): Collection<Item>?

}