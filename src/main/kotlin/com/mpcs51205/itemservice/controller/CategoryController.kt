package com.mpcs51205.itemservice.controller

import com.mpcs51205.itemservice.models.Category
import com.mpcs51205.itemservice.service.CategoryService

import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/category")
class CategoryController(val categoryService: CategoryService) {

    @PostMapping("")
    fun postCategory(@RequestBody category: Category): Category = categoryService.createCategory(category)

    @GetMapping("/{catId}")
    fun getCategory(@PathVariable catId: UUID): Category = categoryService.getCategoryById(catId)

    @PutMapping("/{catId}")
    fun modifyCategory(@PathVariable catId: UUID, @RequestBody newDescription: String) =
        categoryService.modifyCategory(catId, newDescription)

    @DeleteMapping("/{catId}")
    fun deleteCategory(@PathVariable catId: UUID) = categoryService.deleteCategory(catId)
}