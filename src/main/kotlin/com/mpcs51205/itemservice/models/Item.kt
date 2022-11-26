package com.mpcs51205.itemservice.models


import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.annotations.GenericGenerator
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table
class Item: Serializable {

    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "uuid2")
    @Column(nullable = false)
    var id: UUID? = null

    @Column(nullable = false)
    var userId: UUID? = null

    @Column(nullable = false, unique = true)
    var description: String? = null

    @Column(nullable = false)
    var quantity: Int? = null

    @Column(nullable = false)
    var price: Double? = null

    @Column(nullable = false)
    var shippingCosts: Double? = null

    @Column(nullable = false)
    var startTime: Date? = null

    @Column(nullable = false)
    var endTime: Date? = null

    @Column
    var buyNow: Boolean? = null

    @Column
    var upForAuction: Boolean? = null

    @Column
    var counterfeit: Boolean? = null

    @Column
    var inappropriate: Boolean? = null

    @ManyToMany(mappedBy = "items", cascade = [CascadeType.PERSIST])
    var categories = mutableListOf<Category>()

    @OneToMany(mappedBy = "item", cascade = [CascadeType.ALL])
    var bookmarks = mutableListOf<Bookmark>()

    fun isCategoryApplied(catId: UUID): Boolean {
        for (category in this.categories) {
            if (category.id == catId) {
                return true
            }
        }
        return false
    }

    fun isBookmarkApplied(userId: UUID): Boolean {
        for (bookmark in this.bookmarks) {
            if (bookmark.userId == userId) {
                return true
            }
        }
        return false
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class ItemUpdate: Serializable {
    var userId: UUID? = null
    var price: Double? = null
    var quantity: Int? = null
    var startTime: Date? = null
    var endTime: Date? = null
    var shippingCosts: Double? = null
    var description: String? = null
    var buyNow: Boolean? = null
    var upForAuction: Boolean? = null
    var counterfeit: Boolean? = null
    var inappropriate: Boolean? = null
    var categories: MutableList<Category>? = null
    var bookmarks: MutableList<Bookmark>? = null

    fun update(item: Item) : ItemUpdateEvent {
        item.userId = this.userId ?: item.userId
        item.price = this.price ?: item.price
        item.quantity = this.quantity ?: item.quantity
        item.startTime = this.startTime ?: item.startTime
        item.endTime = this.endTime ?: item.endTime
        item.shippingCosts = this.shippingCosts ?: item.shippingCosts
        item.description = this.description ?: item.description
        item.buyNow = this.buyNow ?: item.buyNow
        item.upForAuction = this.upForAuction ?: item.upForAuction
        item.counterfeit = this.counterfeit ?: item.counterfeit
        item.inappropriate = this.inappropriate ?: item.inappropriate
        item.categories = this.categories ?: item.categories
        item.bookmarks = this.bookmarks ?: item.bookmarks

        return ItemUpdateEvent(item.id!!, this)
    }

    fun createQuery(item: Item) {
        item.userId = this.userId ?: item.userId
        item.price = this.price ?: item.price
        item.quantity = this.quantity ?: item.quantity
        item.startTime = this.startTime ?: item.startTime
        item.endTime = this.endTime ?: item.endTime
        item.shippingCosts = this.shippingCosts ?: item.shippingCosts
        item.description = this.description ?: item.description
        item.buyNow = this.buyNow ?: item.buyNow
        item.upForAuction = this.upForAuction ?: item.upForAuction
        item.counterfeit = this.counterfeit ?: item.counterfeit
        item.inappropriate = this.inappropriate ?: item.inappropriate
        item.categories = this.categories ?: item.categories
        item.bookmarks = this.bookmarks ?: item.bookmarks
    }
}

class ItemUpdateEvent(val itemId: UUID, val update: ItemUpdate): Serializable

class AuctionItemTemplate: Serializable {
    lateinit var itemId: UUID
    lateinit var sellerUserId: UUID
    lateinit var startTime: String
    lateinit var endTime: String
    var startPriceInCents: Int? = null

    fun createFromItem(item: Item) {
        this.itemId = item.id!!
        this.sellerUserId = item.userId!!
        this.startTime = item.startTime.toString()
        this.endTime = item.endTime.toString()
        this.startPriceInCents = (item.price!! * 100).toInt()
    }
}
