package com.booknest;

import java.util.List;

public interface WalletService {
    List<Wallet> getWallets();
    Wallet addWallet(Wallet wallet);
    void addMoney(Wallet wallet, double amount, String remarks);
    void update(Wallet wallet, double amount, String remarks, int orderId);
    Wallet getById(int walletId);
    List<Statement> getStatementsById(int walletId);
    List<Statement> getStatements();
    void deleteById(int walletId);
}
