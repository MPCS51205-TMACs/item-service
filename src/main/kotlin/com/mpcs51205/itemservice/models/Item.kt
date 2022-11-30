package com.mpcs51205.itemservice.models

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.hibernate.annotations.GenericGenerator
import java.io.Serializable
import java.time.LocalDateTime
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
    var price: Double? = null   // the 'Buy It Now' price

    @Column(nullable = false)
    var startPrice: Double? = null  // The lowest price for a bid

    @Column(nullable = false)
    var shippingCosts: Double? = null

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS", shape = JsonFormat.Shape.STRING)
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    var startTime: LocalDateTime? = null

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS", shape = JsonFormat.Shape.STRING)
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    var endTime: LocalDateTime? = null

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
    fun createQuery(item: Item) {
        item.price = this.price ?: item.price
        item.startPrice = this.startPrice ?: item.startPrice
        item.quantity = this.quantity ?: item.quantity
        item.startTime = this.startTime ?: item.startTime
        item.endTime = this.endTime ?: item.endTime
        item.shippingCosts = this.shippingCosts ?: item.shippingCosts
        item.description = this.description ?: item.description
        item.buyNow = this.buyNow ?: item.buyNow
        item.upForAuction = this.upForAuction ?: item.upForAuction
        item.counterfeit = this.counterfeit ?: item.counterfeit
        item.inappropriate = this.inappropriate ?: item.inappropriate
        item.categories = this.categories
        item.bookmarks = this.bookmarks
    }
}
