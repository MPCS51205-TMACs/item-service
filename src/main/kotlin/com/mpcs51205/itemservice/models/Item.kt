package com.mpcs51205.itemservice.models


import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.annotations.GenericGenerator
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(indexes = [Index(columnList = "id")])
class Item: Serializable {

    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "uuid2")
    @Column(nullable = false)
    var id: UUID? = null

    @Column(nullable = false)
    var userEmail: String? = null

    @Column(nullable = false)
    lateinit var description: String

    @Column(nullable = false)
    var quantity: Int = 0

    @Column(nullable = false)
    var price: Double = 0.0

    @Column(nullable = false)
    var shippingCosts: Double = 0.0

    @Column(nullable = false)
    lateinit var startTime: Date

    @Column(nullable = false)
    lateinit var endTime: Date

    @Column
    var buyNow: Boolean = true

    @Column
    var upForAuction: Boolean = true

    @Column
    var counterfeit: Boolean = false

    @Column
    var inappropriate: Boolean = false

    @JsonIgnore
    @ManyToMany(mappedBy = "items", cascade = [CascadeType.PERSIST])
    var categories = mutableListOf<ItemCategory>()

    @ManyToMany
    var bookmarks: List<ItemBookmark> = mutableListOf()

    fun isCategoryApplied(catId: UUID): Boolean {
        for (category in this.categories) {
            if (category.id == catId) {
                return true
            }
        }
        return false
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class ItemUpdate: Serializable {
    var userEmail: String? = null
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

    fun update(item: Item) : ItemUpdateEvent {
        item.userEmail = this.userEmail ?: item.userEmail
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

        return ItemUpdateEvent(item.id!!, this)
    }
}

class ItemUpdateEvent(val itemId: UUID, val update: ItemUpdate): Serializable