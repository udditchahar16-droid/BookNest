package com.booknest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {
    Wallet findByWalletId(int walletId);
    @Query("SELECT w FROM Wallet w") List<Wallet> getAllWallets();
    void deleteByWalletId(int walletId);
}
