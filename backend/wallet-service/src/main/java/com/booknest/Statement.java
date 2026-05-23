package com.booknest;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "statements")
public class Statement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int statementId;

    @Column(name = "wallet_id", insertable = false, updatable = false)
    private int walletId;

    private String transactionType;
    private double amount;
    private LocalDateTime dateTime;

    // nullable=true — top-up orders have no orderId (0 default)
    @Column(name = "order_id", nullable = true)
    private Integer orderId;

    private String transactionRemarks;

    public Statement() {}

    public Statement(String type, double amount, LocalDateTime dt, int orderId, String remarks) {
        this.transactionType = type;
        this.amount = amount;
        this.dateTime = dt;
        this.orderId = orderId;
        this.transactionRemarks = remarks;
    }

    public int getStatementId()                    { return statementId; }
    public void setStatementId(int v)              { this.statementId = v; }
    public int getWalletId()                       { return walletId; }
    public void setWalletId(int v)                 { this.walletId = v; }
    public String getTransactionType()             { return transactionType; }
    public void setTransactionType(String v)       { this.transactionType = v; }
    public double getAmount()                      { return amount; }
    public void setAmount(double v)                { this.amount = v; }
    public LocalDateTime getDateTime()             { return dateTime; }
    public void setDateTime(LocalDateTime v)       { this.dateTime = v; }
    public Integer getOrderId()                    { return orderId; }
    public void setOrderId(int v)                  { this.orderId = v; }
    public String getTransactionRemarks()          { return transactionRemarks; }
    public void setTransactionRemarks(String v)    { this.transactionRemarks = v; }
}
