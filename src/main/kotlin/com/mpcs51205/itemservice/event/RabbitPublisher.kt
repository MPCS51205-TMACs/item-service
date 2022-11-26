package com.mpcs51205.itemservice.event

import com.mpcs51205.itemservice.models.Item
import com.mpcs51205.itemservice.models.ItemUpdateEvent

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.UUID


@Component
class RabbitPublisher {
    @Autowired
    lateinit var template: RabbitTemplate

    @Value("\${spring.rabbitmq.template.exchange-create}")
    lateinit var createExchange: String
    @Value("\${spring.rabbitmq.template.exchange-update}")
    lateinit var updateExchange: String
    @Value("item.delete")
    lateinit var deleteExchange: String

    fun sendCreateEvent(item: Item) = send(exchange = createExchange, payload = item)
    fun sendUpdateEvent(itemUpdateEvent: ItemUpdateEvent) = send(exchange = updateExchange, payload = itemUpdateEvent)
    fun sendDeleteEvent(itemId: UUID) = send(exchange = deleteExchange, payload = itemId)

    private fun send(exchange: String, payload: Serializable) {
        template.convertAndSend(exchange, "", payload)
    }
}