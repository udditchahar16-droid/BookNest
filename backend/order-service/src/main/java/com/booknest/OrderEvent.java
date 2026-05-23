package com.booknest;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Event payload published to RabbitMQ by order-service.
 * Consumed by notification-service to create in-app / email alerts.
 */
public class OrderEvent implements Serializable {

    private int     orderId;
    private int     userId;
    private String  eventType;   // ORDER_PLACED | ORDER_STATUS_CHANGED
    private String  orderStatus; // PLACED | CONFIRMED | DISPATCHED | DELIVERED | CANCELLED
    private String  paymentMode; // COD | WALLET
    private double  amountPaid;
    private LocalDate orderDate;

    public OrderEvent() {}

    public OrderEvent(int orderId, int userId, String eventType,
                      String orderStatus, String paymentMode,
                      double amountPaid, LocalDate orderDate) {
        this.orderId     = orderId;
        this.userId      = userId;
        this.eventType   = eventType;
        this.orderStatus = orderStatus;
        this.paymentMode = paymentMode;
        this.amountPaid  = amountPaid;
        this.orderDate   = orderDate;
    }

    // ── Getters & Setters ─────────────────────────────────────
    public int getOrderId()             { return orderId; }
    public void setOrderId(int v)       { this.orderId = v; }

    public int getUserId()              { return userId; }
    public void setUserId(int v)        { this.userId = v; }

    public String getEventType()        { return eventType; }
    public void setEventType(String v)  { this.eventType = v; }

    public String getOrderStatus()      { return orderStatus; }
    public void setOrderStatus(String v){ this.orderStatus = v; }

    public String getPaymentMode()      { return paymentMode; }
    public void setPaymentMode(String v){ this.paymentMode = v; }

    public double getAmountPaid()       { return amountPaid; }
    public void setAmountPaid(double v) { this.amountPaid = v; }

    public LocalDate getOrderDate()     { return orderDate; }
    public void setOrderDate(LocalDate v){ this.orderDate = v; }

    @Override
    public String toString() {
        return "OrderEvent{orderId=" + orderId + ", userId=" + userId +
               ", eventType='" + eventType + "', status='" + orderStatus + "'}";
    }
}
