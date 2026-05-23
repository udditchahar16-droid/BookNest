package com.booknest;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StatementsRepository extends JpaRepository<Statement, Integer> {
    Statement findByStatementId(int id);
    List<Statement> findByWalletId(int walletId);
    List<Statement> findByTransactionType(String type);
    List<Statement> findByOrderId(int orderId);
}
