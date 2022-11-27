package com.mpcs51205.itemservice.service

import com.mpcs51205.itemservice.event.RabbitPublisher
import com.mpcs51205.itemservice.models.*
import com.mpcs51205.itemservice.repository.CategoryRepository
import com.mpcs51205.itemservice.repository.ItemRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

@Service
class ItemService(val itemRepository: ItemRepository,
                  val categoryRepository: CategoryRepository,
                  val categoryService: CategoryService,
                  val rabbitMessenger: RabbitPublisher) {

    fun getItemById(itemId: UUID): Item {
        return itemRepository.findByIdOrNull(itemId) ?: throw Exception("Item $itemId not in database.")
    }

    fun createItem(item: Item): Item {
        // Generate unique item ID first

        saveItem(item)

        // Set up request body and headers
        val headers = org.springframework.http.HttpHeaders()
        val restTemplate = RestTemplate()
        headers.contentType = MediaType.APPLICATION_JSON

        var payload = AuctionItem()
        payload.createFromItem(item)

        val request: HttpEntity<AuctionItem> = HttpEntity<AuctionItem>(payload, headers)
        var response = restTemplate.exchange("http://auctions-service:10000/api/v1/Auctions/",
            HttpMethod.POST, request, AuctionItem::class.java)
        if (response.statusCode.is2xxSuccessful) {
            rabbitMessenger.sendCreateEvent(item)
            return item
        } else {
            deleteItem(item.id!!)
            throw Exception("Auction invalidated item creation.")
        }
    }

    fun saveItem(item: Item) {
        try {
            itemRepository.save(item)
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    fun deleteItem(itemId: UUID) {
        val item: Item = getItemById(itemId)

        // Set up request body and headers
        val headers = org.springframework.http.HttpHeaders()
        val restTemplate = RestTemplate()
        headers.contentType = MediaType.APPLICATION_JSON

        val payload = AuctionDeleteItem(item.userId.toString())
        val request: HttpEntity<AuctionDeleteItem> = HttpEntity<AuctionDeleteItem>(payload, headers)
        var response = restTemplate.exchange("http://auctions-service:10000/api/v1/cancelAuction/${itemId}",
            HttpMethod.POST, request, AuctionItem::class.java)

        if (response.statusCode.is2xxSuccessful) {
            itemRepository.delete(getItemById(itemId))
            rabbitMessenger.sendDeleteEvent(itemId)

        } else {
            throw Exception("Auction invalidated item deletion")
        }
    }

    fun updateItem(updateSrc: ItemUpdate, targetItem: UUID): Item {
        val target: Item = getItemById(targetItem)
        val updateEvent = updateSrc.update(item = target)
        saveItem(item = target)
        rabbitMessenger.sendUpdateEvent(updateEvent)
        return target
    }

    fun addCategoryToItem(itemId: UUID, categoryName: String): List<Category> {
        val item: Item = getItemById(itemId)
        val targetCategory: Category? = categoryRepository.findByCategoryDescriptionIs(categoryName)
        if (targetCategory != null) {
            if (!item.isCategoryApplied(targetCategory.id!!)) {
                item.categories += targetCategory
                targetCategory.items += item
                categoryRepository.save(targetCategory)
            }
        } else {
            val newCategory = Category(categoryName)
            newCategory.items += item
            categoryService.createCategory(newCategory)
            item.categories += newCategory
        }
        rabbitMessenger.sendUpdateEvent(ItemUpdateEvent(itemId, item))
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
        rabbitMessenger.sendUpdateEvent(ItemUpdateEvent(itemId, item))
        return item.bookmarks
    }

    fun getBookmarkedItems(userId: UUID): List<Item> {
        return itemRepository.getItemsByBookmarks(userId) as List<Item>
    }

    fun getUsersByBookmarkedItem(itemId: UUID): List<UUID> {
        return itemRepository.getUsersByBookmarkedItem(itemId) as List<UUID>
    }

    fun queryItems(queryExample: Item): Collection<Item> {
        var queryItem = Item()
        queryExample.createQuery(queryItem)

        val matcher: ExampleMatcher = ExampleMatcher.matchingAll()
            .withMatcher("description", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
            .withIgnorePaths("id", "userEmail", "bookmarks")
        val example: Example<Item> = Example.of(queryItem, matcher)
        var results = itemRepository.findAll(example)

        if (queryItem.categories.isNotEmpty() && results.isNotEmpty()) {
            var queryIds = mutableListOf<String>()
            for (cat in queryItem.categories) { queryIds.add(cat.categoryDescription!!) }
            results.removeAll {
                var resultIds = mutableListOf<String>()
                for (cat in it.categories) { resultIds.add(cat.categoryDescription!!) }
                !resultIds.containsAll(queryIds)
            }
        }
        return results
    }
}
