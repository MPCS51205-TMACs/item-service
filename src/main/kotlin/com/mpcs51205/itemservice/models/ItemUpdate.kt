package com.mpcs51205.itemservice.models

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
class ItemUpdate: Serializable {
    var quantity: Int? = null
    var shippingCosts: Double? = null
    var description: String? = null
    var buyNow: Boolean? = null
    var counterfeit: Boolean? = null
    var inappropriate: Boolean? = null

    fun update(item: Item) : ItemUpdateEvent {
        item.quantity = this.quantity ?: item.quantity
        item.shippingCosts = this.shippingCosts ?: item.shippingCosts
        item.description = this.description ?: item.description
        item.buyNow = this.buyNow ?: item.buyNow
        item.counterfeit = this.counterfeit ?: item.counterfeit
        item.inappropriate = this.inappropriate ?: item.inappropriate

        return ItemUpdateEvent(item.id!!, this)
    }
}

class ItemUpdateEvent(val itemId: UUID, val update: Serializable): Serializable