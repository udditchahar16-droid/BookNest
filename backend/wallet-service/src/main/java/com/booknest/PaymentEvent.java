package com.booknest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event payload published to RabbitMQ by wallet-service.
 * Consumed by notification-service to create payment alerts.
 */
public class PaymentEvent implements Serializable {

    private int           walletId;
    private int           userId;
    private String        eventType;
    private double        amount;
    private double        balanceAfter;
    private int           orderId;
    private String        remarks;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime timestamp;

    public PaymentEvent() {}

    public PaymentEvent(int walletId, int userId, String eventType,
                        double amount, double balanceAfter,
                        int orderId, String remarks) {
        this.walletId     = walletId;
        this.userId       = userId;
        this.eventType    = eventType;
        this.amount       = amount;
        this.balanceAfter = balanceAfter;
        this.orderId      = orderId;
        this.remarks      = remarks;
        this.timestamp    = LocalDateTime.now();
    }

    public int getWalletId()              { return walletId; }
    public void setWalletId(int v)        { this.walletId = v; }

    public int getUserId()                { return userId; }
    public void setUserId(int v)          { this.userId = v; }

    public String getEventType()          { return eventType; }
    public void setEventType(String v)    { this.eventType = v; }

    public double getAmount()             { return amount; }
    public void setAmount(double v)       { this.amount = v; }

    public double getBalanceAfter()       { return balanceAfter; }
    public void setBalanceAfter(double v) { this.balanceAfter = v; }

    public int getOrderId()               { return orderId; }
    public void setOrderId(int v)         { this.orderId = v; }

    public String getRemarks()            { return remarks; }
    public void setRemarks(String v)      { this.remarks = v; }

    public LocalDateTime getTimestamp()   { return timestamp; }
    public void setTimestamp(LocalDateTime v){ this.timestamp = v; }

    @Override
    public String toString() {
        return "PaymentEvent{walletId=" + walletId + ", userId=" + userId +
                ", eventType='" + eventType + "', amount=" + amount + "}";
    }
}