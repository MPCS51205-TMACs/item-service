package com.mpcs51205.itemservice.service

import com.mpcs51205.itemservice.event.RabbitPublisher
import com.mpcs51205.itemservice.models.*
import com.mpcs51205.itemservice.repository.CategoryRepository
import com.mpcs51205.itemservice.repository.ItemRepository
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
        return itemRepository.findByIdOrNull(itemId) ?: throw Exception("NOT FOUND: Item $itemId not in database.")
    }

    fun getItemsbyUserId(userId: UUID): List<UUID> {
        return itemRepository.getItemsByUser(userId) as List<UUID>
    }

    fun createItem(item: Item): Item {
        // Generate unique item ID first
        saveItem(item)

        // Set up request body and headers
        val headers = org.springframework.http.HttpHeaders()
        val restTemplate = RestTemplate()
        headers.contentType = MediaType.APPLICATION_JSON

        val payload = AuctionItem()
        payload.createFromItem(item)

        val request: HttpEntity<AuctionItem> = HttpEntity<AuctionItem>(payload, headers)
        try {
            val response = restTemplate.exchange("http://auctions-service:10000/api/v1/Auctions/",
                HttpMethod.POST, request, AuctionItem::class.java)
            if (response.statusCode.is2xxSuccessful) {
                println("ITEM CREATED: Item ${item.id} created and auction created.")
                rabbitMessenger.sendCreateEvent(item)
                return item
            }
        } catch(e: Exception) {
            itemRepository.delete(item)
            throw Exception("ITEM NOT CREATED: Auction invalidated item creation. Reason: ${e.message}")
        }
        return Item()
    }

    fun saveItem(item: Item) {
        try {
            itemRepository.save(item)
        } catch (e: Exception) {
            throw Exception("ERROR SAVING ITEM: ${e.message}")
        }
    }

    fun deleteItem(userId: UUID, itemId: UUID) {
        val item: Item = getItemById(itemId)
        if (userId == item.userId) {
            // Set up request body and headers
            val headers = org.springframework.http.HttpHeaders()
            val restTemplate = RestTemplate()
            headers.contentType = MediaType.APPLICATION_JSON

            val payload = AuctionDeleteItem(item.userId.toString())
            val request: HttpEntity<AuctionDeleteItem> = HttpEntity<AuctionDeleteItem>(payload, headers)
            try {
                val response = restTemplate.exchange(
                    "http://auctions-service:10000/api/v1/cancelAuction/${itemId}",
                    HttpMethod.POST, request, AuctionItem::class.java
                )
                if (response.statusCode.is2xxSuccessful) {
                    println("ITEM DELETED: Item $itemId deleted successfully and auction cancelled.")
                    itemRepository.delete(getItemById(itemId))
                    rabbitMessenger.sendDeleteEvent(itemId)
                }
            } catch (e: Exception) {
                throw Exception("ITEM DELETION INVALIDATED: Auction invalidated item deletion")
            }
        } else {
            throw Exception("NOT AUTHORIZED TO DELETE ITEM")
        }
    }

    fun updateItem(updateSrc: ItemUpdate, targetItem: UUID, userId: UUID): Item {
        val target: Item = getItemById(targetItem)
        if (userId == target.userId) {
            val updateEvent = updateSrc.update(item = target)
            saveItem(item = target)
            rabbitMessenger.sendUpdateEvent(updateEvent)
            println("ITEM UPDATED")
            return target
        } else {
            throw Exception("NOT AUTHORIZED TO UPDATE ITEM")
        }
    }

    fun markItemInappropriate(itemId: UUID): UUID {
        val target: Item = getItemById(itemId)
        target.inappropriate = true
        saveItem(item = target)
        rabbitMessenger.sendInappropriateEvent(itemId)
        println("MARKED INAPPROPRIATE")
        return itemId
    }

    fun markItemCounterfeit(itemId: UUID): UUID {
        val target: Item = getItemById(itemId)
        target.counterfeit = true
        saveItem(item = target)
        rabbitMessenger.sendCounterfeitEvent(itemId)
        return itemId
    }

    fun addCategoryToItem(itemId: UUID, categoryName: String, userId: UUID): List<Category> {
        val item: Item = getItemById(itemId)
        if (item.userId == userId) {
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
            saveItem(item)
            rabbitMessenger.sendUpdateEvent(ItemUpdateEvent(itemId, item))
            return item.categories
        } else {
            throw Exception("NOT AUTHORIZED TO UPDATE ITEM")
        }
    }

    fun addBookmarkToItem(itemId: UUID, userId: UUID): List<Bookmark> {
        val item: Item = getItemById(itemId)
        if (item.userId == userId) {
            if (!item.isBookmarkApplied(userId)) {
                val newBookmark = Bookmark()
                newBookmark.userId = userId
                newBookmark.item = item
                println("CREATED A NEW BOOKMARK FOR USER: $userId")
                item.bookmarks += newBookmark
            }
            saveItem(item)
            rabbitMessenger.sendUpdateEvent(ItemUpdateEvent(itemId, item))
            return item.bookmarks
        } else {
            throw Exception("NOT AUTHORIZED TO UPDATE ITEM")
        }
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

    fun getItemsFromList(idList: List<String>): Collection<Item> {
        return idList.map { itemRepository.getItemById(UUID.fromString(it))!! }
    }

    fun checkoutItems(idList: List<String>) {
        for (id in idList) {
            itemRepository.delete(getItemById(UUID.fromString(id)))
            println("ITEM CHECKED OUT")
        }
    }

    fun getAllItems(): Collection<Item> {
        return itemRepository.findAll()
    }

    fun getMarkedItems(): Collection<Item> {
        val markedItems = itemRepository.getMarkedItems()
        return if (!markedItems.isNullOrEmpty()) {
            markedItems
        } else {
            listOf()
        }
    }
}
