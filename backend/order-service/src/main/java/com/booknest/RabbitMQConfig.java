package com.booknest;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for order-service (Producer).
 *
 * Exchange  : booknest.events  (topic exchange)
 * Routing keys published by order-service:
 *   order.placed   – when a new order is saved (COD or WALLET)
 *   order.status   – when admin changes order status
 */
@Configuration
public class RabbitMQConfig {

    // ── Exchange ──────────────────────────────────────────────
    public static final String EXCHANGE = "booknest.events";

    // ── Routing keys (producer side) ─────────────────────────
    public static final String RK_ORDER_PLACED = "order.placed";
    public static final String RK_ORDER_STATUS = "order.status";

    @Bean
    public TopicExchange bookNestExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // ── Jackson JSON converter ────────────────────────────────
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
