package com.mpcs51205.itemservice.controller

import com.mpcs51205.itemservice.models.Item
import com.mpcs51205.itemservice.models.ItemUpdate
import com.mpcs51205.itemservice.service.ItemService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/item")
class ItemController(val itemService: ItemService) {

    @PostMapping("")
    fun postItem(@RequestBody item: Item): Item = itemService.createItem(item)

    @GetMapping("/{itemId}")
    fun getItem(@PathVariable itemId: UUID): Item = itemService.getItemById(itemId)

    @GetMapping("/query")
    fun queryItems(@RequestBody queryExample: Item): Collection<Item> = itemService.queryItems(queryExample)

    @GetMapping("/bookmark/byUser:{userId}")
    fun getBookmarkedItemsByUser(@PathVariable userId: UUID): Collection<Item> =
        itemService.getBookmarkedItems(userId)

    @GetMapping("/bookmark/byItem:{itemId}")
    fun getUsersByBookmarkedItem(@PathVariable itemId: UUID): Collection<UUID> =
        itemService.getUsersByBookmarkedItem(itemId)

    @DeleteMapping("/{itemId}")
    fun deleteItem(@PathVariable itemId: UUID) = itemService.deleteItem(itemId)

    @PutMapping("/{itemId}")
    fun updateItem(@RequestBody itemUpdate: ItemUpdate, @PathVariable itemId: UUID) =
        itemService.updateItem(itemUpdate, itemId)

    @PutMapping("/category/{itemId}")
    fun addCategoryToItem(@PathVariable itemId: UUID, @RequestBody newCategoryName: String) =
        itemService.addCategoryToItem(itemId, newCategoryName)

    @PutMapping("/bookmark/{itemId}")
    fun addBookmarkToItem(@PathVariable itemId: UUID, @RequestBody userId: UUID) =
        itemService.addBookmarkToItem(itemId, userId)
}