package com.mpcs51205.itemservice.event

import com.mpcs51205.itemservice.models.Item
import com.mpcs51205.itemservice.models.ItemUpdateEvent
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.*


@Component
class RabbitPublisher {
    @Autowired
    lateinit var template: RabbitTemplate

    @Autowired
    lateinit var rabbitConfig: RabbitConfig

    fun sendCreateEvent(item: Item) = send(exchange = rabbitConfig.createExchange, payload = item)
    fun sendUpdateEvent(itemUpdateEvent: ItemUpdateEvent) = send(exchange = rabbitConfig.updateExchange, payload = itemUpdateEvent)
    fun sendDeleteEvent(itemId: UUID) = send(exchange = rabbitConfig.deleteExchange, payload = itemId)
    fun sendInappropriateEvent(itemId: UUID) = send(exchange = rabbitConfig.inappropriateExchange, payload = itemId)
    fun sendCounterfeitEvent(itemId: UUID) = send(exchange = rabbitConfig.counterfeitExchange, payload = itemId)

    private fun send(exchange: String, payload: Serializable) {
        template.convertAndSend(exchange, "", payload)
    }
}