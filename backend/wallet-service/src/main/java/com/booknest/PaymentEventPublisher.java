package com.booknest;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishPaymentSuccess(PaymentEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RK_PAYMENT_SUCCESS,
                    event
            );
            System.out.println("[WALLET-SERVICE] Published PAYMENT_SUCCESS → " + event);
        } catch (Exception e) {
            System.out.println("[WALLET-SERVICE] RabbitMQ unavailable, skipping PAYMENT_SUCCESS: " + e.getMessage());
        }
    }

    public void publishPaymentFailure(PaymentEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RK_PAYMENT_FAILURE,
                    event
            );
            System.out.println("[WALLET-SERVICE] Published PAYMENT_FAILURE → " + event);
        } catch (Exception e) {
            System.out.println("[WALLET-SERVICE] RabbitMQ unavailable, skipping PAYMENT_FAILURE: " + e.getMessage());
        }
    }

    public void publishTopUp(PaymentEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RK_PAYMENT_TOPUP,
                    event
            );
            System.out.println("[WALLET-SERVICE] Published PAYMENT_TOPUP → " + event);
        } catch (Exception e) {
            System.out.println("[WALLET-SERVICE] RabbitMQ unavailable, skipping PAYMENT_TOPUP: " + e.getMessage());
        }
    }
}