package com.booknest;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wallets")
public class Wallet {

    // FIX: @GeneratedValue hatao — walletId == userId hona chahiye BookNest mein
    // Agar @GeneratedValue raha toh createWallet mein passed userId ignore ho jaata
    // aur userId=5 ka wallet walletId=1 ban jaata — addMoney /wallet/5/add fail
    @Id
    private int walletId;

    private double currentBalance;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "wallet_id")
    private List<Statement> statements = new ArrayList<>();

    public Wallet() {}

    public int getWalletId()                             { return walletId; }
    public void setWalletId(int walletId)                { this.walletId = walletId; }
    public double getCurrentBalance()                     { return currentBalance; }
    public void setCurrentBalance(double currentBalance)  { this.currentBalance = currentBalance; }
    public List<Statement> getStatements()                { return statements; }
    public void setStatements(List<Statement> statements) { this.statements = statements; }
}
