package com.booknest;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for notification-service (Consumer).
 *
 * Exchange : booknest.events  (topic exchange — same as producers)
 *
 * Queues & bindings:
 *  ┌──────────────────────────────────┬───────────────────────┐
 *  │ Queue                            │ Routing key pattern   │
 *  ├──────────────────────────────────┼───────────────────────┤
 *  │ booknest.notification.order      │ order.*               │
 *  │ booknest.notification.payment    │ payment.*             │
 *  └──────────────────────────────────┴───────────────────────┘
 *
 * order.*   catches: order.placed, order.status
 * payment.* catches: payment.success, payment.failure, payment.topup
 */
@Configuration
public class RabbitMQConfig {

    // ── Exchange ──────────────────────────────────────────────
    public static final String EXCHANGE = "booknest.events";

    // ── Queues ────────────────────────────────────────────────
    public static final String QUEUE_ORDER   = "booknest.notification.order";
    public static final String QUEUE_PAYMENT = "booknest.notification.payment";

    @Bean
    public TopicExchange bookNestExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // ── Queue declarations ────────────────────────────────────
    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(QUEUE_ORDER).build();
    }

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(QUEUE_PAYMENT).build();
    }

    // ── Bindings ──────────────────────────────────────────────
    @Bean
    public Binding orderBinding(Queue orderQueue, TopicExchange bookNestExchange) {
        return BindingBuilder.bind(orderQueue).to(bookNestExchange).with("order.*");
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, TopicExchange bookNestExchange) {
        return BindingBuilder.bind(paymentQueue).to(bookNestExchange).with("payment.*");
    }

    // ── JSON converter ────────────────────────────────────────
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
