package com.booknest;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Mirror of the OrderEvent published by order-service.
 * Used for JSON deserialization in the notification-service consumer.
 */
public class OrderEvent implements Serializable {

    private int       orderId;
    private int       userId;
    private String    eventType;
    private String    orderStatus;
    private String    paymentMode;
    private double    amountPaid;
    private LocalDate orderDate;

    public OrderEvent() {}

    public int getOrderId()              { return orderId; }
    public void setOrderId(int v)        { this.orderId = v; }
    public int getUserId()               { return userId; }
    public void setUserId(int v)         { this.userId = v; }
    public String getEventType()         { return eventType; }
    public void setEventType(String v)   { this.eventType = v; }
    public String getOrderStatus()       { return orderStatus; }
    public void setOrderStatus(String v) { this.orderStatus = v; }
    public String getPaymentMode()       { return paymentMode; }
    public void setPaymentMode(String v) { this.paymentMode = v; }
    public double getAmountPaid()        { return amountPaid; }
    public void setAmountPaid(double v)  { this.amountPaid = v; }
    public LocalDate getOrderDate()      { return orderDate; }
    public void setOrderDate(LocalDate v){ this.orderDate = v; }
}
