package com.mpcs51205.itemservice.event

import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class RabbitConfig {

    @Bean
    fun jsonMessageConverter(): MessageConverter? {
        return Jackson2JsonMessageConverter()
    }

    @Value("\${spring.rabbitmq.template.exchange-create}")
    lateinit var createExchange: String
    @Value("\${spring.rabbitmq.template.exchange-update}")
    lateinit var updateExchange: String
    @Value("\${spring.rabbitmq.template.exchange-delete}")
    lateinit var deleteExchange: String
    @Value("\${spring.rabbitmq.template.exchange-inappropriate}")
    lateinit var inappropriateExchange: String
    @Value("\${spring.rabbitmq.template.exchange-counterfeit}")
    lateinit var counterfeitExchange: String

    @Bean
    fun itemCreateExchange() = FanoutExchange(createExchange, true, false)
    @Bean
    fun itemUpdateExchange() = FanoutExchange(updateExchange, true, false)
    @Bean
    fun itemDeleteExchange() = FanoutExchange(deleteExchange, true, false)
    @Bean
    fun itemInappropriateExchange() = FanoutExchange(inappropriateExchange, true, false)
    @Bean
    fun itemCounterfeitExchange() = FanoutExchange(counterfeitExchange, true, false)
}