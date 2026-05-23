package com.booknest;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Mirror of the PaymentEvent published by wallet-service.
 * Used for JSON deserialization in the notification-service consumer.
 */
public class PaymentEvent implements Serializable {

    private int           walletId;
    private int           userId;
    private String        eventType;
    private double        amount;
    private double        balanceAfter;
    private int           orderId;
    private String        remarks;
    private LocalDateTime timestamp;

    public PaymentEvent() {}

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
}
