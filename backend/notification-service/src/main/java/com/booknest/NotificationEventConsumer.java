package com.booknest;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * RabbitMQ Consumer — notification-service
 *
 * Queue 1: booknest.notification.order   → order.placed, order.status
 * Queue 2: booknest.notification.payment → payment.success, payment.failure, payment.topup
 *
 * Flow:
 *   1. Event receive karo RabbitMQ se
 *   2. Notification DB mein save karo (in-app)
 *   3. User ka real email auth-service se fetch karo
 *   4. Email bhejo via SMTP
 */
@Component
public class NotificationEventConsumer {

    @Autowired private NotificationService notificationService;
    @Autowired private RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    // ── ORDER QUEUE ───────────────────────────────────────────
    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER)
    public void handleOrderEvent(OrderEvent event) {
        System.out.println("[NOTIFICATION] OrderEvent: " + event.getEventType()
            + " | orderId=" + event.getOrderId() + " | userId=" + event.getUserId());

        String type, message;

        switch (event.getEventType()) {
            case "ORDER_PLACED":
                type    = "ORDER_PLACED";
                message = "Your order #" + event.getOrderId()
                        + " has been placed successfully via " + event.getPaymentMode()
                        + (event.getAmountPaid() > 0 ? ". Amount: ₹" + event.getAmountPaid() : "")
                        + ". Status: PLACED.";
                break;
            case "ORDER_STATUS_CHANGED":
                type    = "ORDER_STATUS_" + event.getOrderStatus();
                message = buildStatusMessage(event);
                break;
            default:
                type    = event.getEventType();
                message = "Order update received for order #" + event.getOrderId();
        }

        // 1. In-app notification save
        notificationService.sendNotification(
            new Notification(event.getUserId(), type, message));

        // 2. Email bhejo — real email auth-service se fetch karo
        String email = fetchUserEmail(event.getUserId());
        if (email != null) {
            notificationService.sendEmailAlert(
                email,
                "BookNest — " + friendlyType(type),
                message
            );
        }
    }

    // ── PAYMENT QUEUE ─────────────────────────────────────────
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT)
    public void handlePaymentEvent(PaymentEvent event) {
        System.out.println("[NOTIFICATION] PaymentEvent: " + event.getEventType()
            + " | userId=" + event.getUserId() + " | amount=" + event.getAmount());

        String type, message;

        switch (event.getEventType()) {
            case "PAYMENT_SUCCESS":
                type    = "PAYMENT_SUCCESS";
                message = "Payment of ₹" + event.getAmount()
                        + " deducted for order #" + event.getOrderId()
                        + ". Wallet balance: ₹" + event.getBalanceAfter();
                break;
            case "PAYMENT_FAILURE":
                type    = "PAYMENT_FAILURE";
                message = "Payment failed for order #" + event.getOrderId()
                        + ". Insufficient wallet balance. Current balance: ₹" + event.getBalanceAfter();
                break;
            case "PAYMENT_TOPUP":
                type    = "PAYMENT_TOPUP";
                message = "₹" + event.getAmount() + " added to your wallet."
                        + " New balance: ₹" + event.getBalanceAfter();
                break;
            default:
                type    = event.getEventType();
                message = "Wallet update: " + event.getRemarks();
        }

        // 1. In-app notification save — userId = walletId (BookNest convention)
        notificationService.sendNotification(
            new Notification(event.getUserId(), type, message));

        // 2. Email bhejo
        String email = fetchUserEmail(event.getUserId());
        if (email != null) {
            notificationService.sendEmailAlert(
                email,
                "BookNest — " + friendlyType(type),
                message
            );
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    /**
     * Auth-service se user ka email fetch karta hai.
     * Agar auth-service down hai toh null return karta hai (graceful degradation).
     */
    private String fetchUserEmail(int userId) {
        if (userId <= 0) return null;
        try {
            Map<?, ?> user = restTemplate.getForObject(
                authServiceUrl + "/api/auth/user/" + userId, Map.class);
            if (user != null && user.get("email") != null) {
                return user.get("email").toString();
            }
        } catch (Exception e) {
            System.err.println("[NOTIFICATION] Could not fetch email for userId="
                + userId + ": " + e.getMessage());
        }
        return null;
    }

    private String buildStatusMessage(OrderEvent e) {
        switch (e.getOrderStatus()) {
            case "CONFIRMED":  return "Your order #" + e.getOrderId() + " has been confirmed and is being processed.";
            case "DISPATCHED": return "Great news! Your order #" + e.getOrderId() + " has been dispatched. Get ready!";
            case "DELIVERED":  return "Your order #" + e.getOrderId() + " has been delivered. Enjoy reading! 📚";
            case "CANCELLED":  return "Your order #" + e.getOrderId() + " has been cancelled.";
            default:           return "Your order #" + e.getOrderId() + " status updated to: " + e.getOrderStatus();
        }
    }

    private String friendlyType(String type) {
        if (type == null) return "";
        String s = type.replace("_", " ").toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
