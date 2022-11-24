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
    @Query("SELECT DISTINCT b.item from Bookmark b WHERE b.userId = ?1")
    fun getItemsByBookmarks(userId: UUID): Collection<Item>?

    @Query("SELECT DISTINCT b.userId FROM Bookmark b WHERE b.item.id = ?1")
    fun getUsersByBookmarkedItem(itemId: UUID): Collection<UUID>?
}