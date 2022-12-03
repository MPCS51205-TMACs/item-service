package com.mpcs51205.itemservice.controller

import com.mpcs51205.itemservice.models.Item
import com.mpcs51205.itemservice.models.ItemUpdate
import com.mpcs51205.itemservice.service.ItemService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/item")
class ItemController(val itemService: ItemService) {

    @PostMapping("")
    fun postItem(@RequestBody item: Item, authentication: Authentication): Item = itemService.createItem(item.apply {
        userId = UUID.fromString(authentication.name) })

    @GetMapping("/{itemId}")
    fun getItem(@PathVariable itemId: UUID): Item = itemService.getItemById(itemId)

    @GetMapping("/query")
    fun queryItems(@RequestBody queryExample: Item): Collection<Item> = itemService.queryItems(queryExample)

    @GetMapping("/bookmark/byUser")
    fun getBookmarkedItemsByUser(authentication: Authentication): Collection<Item> =
        itemService.getBookmarkedItems(UUID.fromString(authentication.name))

    @GetMapping("/bookmark/byItem:{itemId}")
    fun getUsersByBookmarkedItem(@PathVariable itemId: UUID): Collection<UUID> =
        itemService.getUsersByBookmarkedItem(itemId)

    @GetMapping("/list")
    fun getItemsFromList(@RequestBody idList: List<String>): Collection<Item> = itemService.getItemsFromList(idList)

    @GetMapping("/all")
    fun getAllItems(): Collection<Item> = itemService.getAllItems()

    @DeleteMapping("/{itemId}")
    fun deleteItem(@PathVariable itemId: UUID, authentication: Authentication) = itemService.deleteItem(UUID.fromString(authentication.name), itemId)

    @DeleteMapping("/checkout")
    fun checkoutItems(@RequestBody idList: List<String>) = itemService.checkoutItems(idList)

    @PutMapping("/{itemId}")
    fun updateItem(@RequestBody itemUpdate: ItemUpdate, @PathVariable itemId: UUID, authentication: Authentication) =
        itemService.updateItem(itemUpdate, itemId, UUID.fromString(authentication.name))
    @PutMapping("/inappropriate/{itemId}")
    fun markItemInappropriate(@PathVariable itemId: UUID) = itemService.markItemInappropriate(itemId)

    @PutMapping("/counterfeit/{itemId}")
    fun markItemCounterfeit(@PathVariable itemId: UUID) = itemService.markItemCounterfeit(itemId)

    @PutMapping("/category/{itemId}")
    fun addCategoryToItem(@PathVariable itemId: UUID, @RequestBody newCategoryName: String, authentication: Authentication) =
        itemService.addCategoryToItem(itemId, newCategoryName, UUID.fromString(authentication.name))

    @PutMapping("/bookmark/{itemId}")
    fun addBookmarkToItem(@PathVariable itemId: UUID, authentication: Authentication) =
        itemService.addBookmarkToItem(itemId, UUID.fromString(authentication.name))
}