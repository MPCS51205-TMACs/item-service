package com.mpcs51205.itemservice.models

import java.io.Serializable
import java.time.format.DateTimeFormatter
import java.util.*

class AuctionItem: Serializable {
    var itemId: UUID? = null
    var sellerUserId: UUID? = null
    var startTime: String? = null
    var endTime: String? = null
    var startPriceInCents: Int? = null

    fun createFromItem(item: Item) {
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
        this.itemId = item.id!!
        this.sellerUserId = item.userId!!
        this.startTime = item.startTime?.format(formatter)
        this.endTime = item.endTime?.format(formatter)
        this.startPriceInCents = (item.price!! * 100).toInt()
    }
}

data class AuctionDeleteItem(val requesterUserId: String): Serializable