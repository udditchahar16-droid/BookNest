package com.booknest;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Publishes order-related events to the RabbitMQ topic exchange.
 *
 * Routing keys:
 *   order.placed  → notification-service creates ORDER_PLACED notification
 *   order.status  → notification-service creates ORDER_STATUS_CHANGED notification
 */
@Component
public class OrderEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Called after a new order (COD or WALLET) is persisted.
     */
    public void publishOrderPlaced(OrderEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.RK_ORDER_PLACED,
                event
            );
            System.out.println("[ORDER-SERVICE] Published ORDER_PLACED → " + event);
        } catch (Exception e) {
            System.out.println("[ORDER-SERVICE] RabbitMQ unavailable, skipping ORDER_PLACED: " + e.getMessage());
        }
    }

    /**
     * Called when admin updates the order status.
     */
    public void publishOrderStatusChanged(OrderEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.RK_ORDER_STATUS,
                event
            );
            System.out.println("[ORDER-SERVICE] Published ORDER_STATUS_CHANGED → " + event);
        } catch (Exception e) {
            System.out.println("[ORDER-SERVICE] RabbitMQ unavailable, skipping ORDER_STATUS_CHANGED: " + e.getMessage());
        }
    }
}
