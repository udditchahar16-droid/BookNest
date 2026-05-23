package com.booknest;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for wallet-service (Producer).
 *
 * Exchange  : booknest.events  (topic exchange — same as order-service)
 * Routing keys published by wallet-service:
 *   payment.success  – wallet debit successful
 *   payment.failure  – wallet debit failed (insufficient balance)
 *   payment.topup    – wallet top-up (addMoney) successful
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE            = "booknest.events";
    public static final String RK_PAYMENT_SUCCESS  = "payment.success";
    public static final String RK_PAYMENT_FAILURE  = "payment.failure";
    public static final String RK_PAYMENT_TOPUP    = "payment.topup";

    @Bean
    public TopicExchange bookNestExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(jsonMessageConverter());
        return tpl;
    }
}
