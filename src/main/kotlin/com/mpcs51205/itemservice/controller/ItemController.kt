package com.mpcs51205.itemservice.controller

import com.mpcs51205.itemservice.event.RabbitPublisher
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

    @DeleteMapping("/{itemId}")
    fun deleteItem(@PathVariable itemId: UUID) = itemService.deleteItem(itemId)

    @PutMapping("/{itemId}")
    fun updateItem(@RequestBody itemUpdate: ItemUpdate, @PathVariable itemId: UUID) =
        itemService.updateItem(itemUpdate, itemId)

    @PutMapping("/{itemId}/{newCat}")
    fun addCategoryToItem(@PathVariable itemId: UUID, @PathVariable newCat: String) =
        itemService.addCategoryToItem(itemId, newCat)
}