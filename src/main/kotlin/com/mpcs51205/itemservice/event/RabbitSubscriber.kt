package com.mpcs51205.itemservice.event

import com.mpcs51205.itemservice.repository.ItemRepository
import com.mpcs51205.itemservice.service.ItemService
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RabbitSubscriber(val itemService: ItemService, val itemRepo: ItemRepository) {

    @Value("\${spring.rabbitmq.template.exchange-user-delete}")
    lateinit var userDeleteExchange: String

    @Bean
    fun userDeleteQueue(): Queue = Queue("item-service:user.delete", true)
    @Bean
    fun userDeleteExchange(): FanoutExchange = FanoutExchange(userDeleteExchange, true, false)
    @Bean
    fun userDeleteBinding(): Binding = Binding(userDeleteQueue().name, Binding.DestinationType.QUEUE,
        userDeleteExchange().name, null, null)

    @RabbitListener(queues = ["item-service:user.delete"])
    fun receive(userId: UUID) {
        println("RECEIVED USER DELETION EVENT")
        val itemsToDelete = itemRepo.getItemsByUser(userId)
        if (itemsToDelete != null) {
            for (item in itemsToDelete) {
                itemService.deleteItem(item)
                println("DELETING: $item")
            }
        }
    }
    @Bean
    fun jsonMessageConverter(): MessageConverter? {
        return Jackson2JsonMessageConverter()
    }
}