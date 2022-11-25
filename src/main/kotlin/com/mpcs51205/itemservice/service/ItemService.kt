package com.mpcs51205.itemservice.service

import com.mpcs51205.itemservice.event.RabbitPublisher
import com.mpcs51205.itemservice.models.Bookmark
import com.mpcs51205.itemservice.models.Item
import com.mpcs51205.itemservice.models.Category
import com.mpcs51205.itemservice.models.ItemUpdate
import com.mpcs51205.itemservice.repository.CategoryRepository
import com.mpcs51205.itemservice.repository.ItemRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.awt.print.Book
import java.util.UUID

@Service
class ItemService(val itemRepository: ItemRepository,
                  val categoryRepository: CategoryRepository,
                  val categoryService: CategoryService,
                  val rabbitMessenger: RabbitPublisher) {

    fun getItemById(itemId: UUID): Item {
        return itemRepository.findByIdOrNull(itemId) ?: throw Exception("Item not in database")
    }

    fun createItem(item: Item): Item {
        saveItem(item)
        rabbitMessenger.sendCreateEvent(item)
        return item
    }

    fun saveItem(item: Item) {
        try {
            itemRepository.save(item)
        } catch (e: DataIntegrityViolationException) {
            throw e
        }
    }

    fun deleteItem(itemId: UUID) = itemRepository.delete(getItemById(itemId))

    fun updateItem(updateSrc: ItemUpdate, targetItem: UUID): Item {
        val target: Item = getItemById(targetItem)
        val updateEvent = updateSrc.update(item = target)
        saveItem(item = target)
        rabbitMessenger.sendUpdateEvent(updateEvent)
        return target
    }

    fun addCategoryToItem(itemId: UUID, newCat: String): List<Category> {
        val item: Item = getItemById(itemId)
        val targetCategory: Category? = categoryRepository.findByCategoryDescriptionIs(newCat)
        if (targetCategory != null) {
            if (!item.isCategoryApplied(targetCategory.id!!)) {
                item.categories += targetCategory
                targetCategory.items += item
                categoryRepository.save(targetCategory)
            }
        } else {
            val newCategory = Category(newCat)
            newCategory.items += item
            categoryService.createCategory(newCategory)
            item.categories += newCategory
        }
        var updateSrc = ItemUpdate()
        updateSrc.categories = item.categories
        updateItem(updateSrc, itemId)
        return item.categories
    }

    fun addBookmarkToItem(itemId: UUID, userId: UUID): List<Bookmark> {
        val item: Item = getItemById(itemId)
        if (!item.isBookmarkApplied(userId)) {
            val newBookmark = Bookmark()
            newBookmark.userId = userId
            newBookmark.item = item
            item.bookmarks += newBookmark
        }
        var updateSrc = ItemUpdate()
        updateSrc.bookmarks = item.bookmarks
        updateItem(updateSrc, itemId)
        return item.bookmarks
    }

    fun getBookmarkedItems(userId: UUID): List<Item> {
        return itemRepository.getItemsByBookmarks(userId) as List<Item>
    }

    fun getUsersByBookmarkedItem(itemId: UUID): List<UUID> {
        return itemRepository.getUsersByBookmarkedItem(itemId) as List<UUID>
    }

    fun queryItems(queryExample: ItemUpdate): Collection<Item> {
        var queryItem = Item()
        queryExample.createQuery(queryItem)

        val matcher: ExampleMatcher = ExampleMatcher.matchingAll()
            .withMatcher("description", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
            .withIgnorePaths("id", "userEmail", "bookmarks")
        val example: Example<Item> = Example.of(queryItem, matcher)
        var results = itemRepository.findAll(example)

        if (queryItem.categories.isNotEmpty() && results.isNotEmpty()) {
            var queryIds = mutableListOf<UUID>()
            for (cat in queryItem.categories) { queryIds.add(cat.id!!) }
            results.removeAll {
                var resultIds = mutableListOf<UUID>()
                for (cat in it.categories) { resultIds.add(cat.id!!) }
                !resultIds.containsAll(queryIds)
            }
        }
        return results
    }
}
