package com.mpcs51205.itemservice.event

import com.mpcs51205.itemservice.repository.ItemRepository
import com.mpcs51205.itemservice.service.ItemService
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.*

@Component
class RabbitSubscriber(val itemService: ItemService) {

    @Bean
    fun jsonMessageConverter(): MessageConverter? {
        return Jackson2JsonMessageConverter()
    }
    @Bean
    fun userDeleteQueue(): Queue = Queue("item-service:user.delete", true)
    @Bean
    fun userDeleteExchange(): FanoutExchange = FanoutExchange("user.delete", true, false)
    @Bean
    fun userDeleteBinding(): Binding = Binding(userDeleteQueue().name, Binding.DestinationType.QUEUE,
        userDeleteExchange().name, "", null)

    @RabbitListener(queues = ["item-service:user.delete"])
    fun receive(userId: UUID) {
        println("RECEIVED USER DELETION EVENT")
        val itemsToDelete = itemService.getItemsbyUserId(userId)
        if (itemsToDelete != null) {
            for (itemId in itemsToDelete) {
                itemService.deleteItem(itemId)
                println("DELETING: $itemId")
            }
        }
    }
}