package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired private WalletRepository     walletRepository;
    @Autowired private StatementsRepository statementsRepository;
    @Autowired private PaymentEventPublisher eventPublisher;   // ← RabbitMQ producer

    @Override public List<Wallet> getWallets() { return walletRepository.getAllWallets(); }
    @Override public Wallet addWallet(Wallet wallet) { return walletRepository.save(wallet); }

    // ── Top-up wallet ─────────────────────────────────────────
    @Override
    public void addMoney(Wallet wallet, double amount, String remarks) {
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
        wallet.setCurrentBalance(wallet.getCurrentBalance() + amount);
        Statement s = new Statement("DEPOSIT", amount, LocalDateTime.now(), 0, remarks);
        wallet.getStatements().add(s);
        walletRepository.save(wallet);

        // Publish PAYMENT_TOPUP event → notification-service
        // walletId == userId in BookNest (wallet created with userId as walletId)
        PaymentEvent event = new PaymentEvent(
            wallet.getWalletId(), wallet.getWalletId(),
            "PAYMENT_TOPUP", amount, wallet.getCurrentBalance(), 0, remarks
        );
        eventPublisher.publishTopUp(event);
    }

    // ── Debit wallet for purchase ─────────────────────────────
    @Override
    public void update(Wallet wallet, double amount, String remarks, int orderId) {
        if (wallet.getCurrentBalance() < amount) {
            // Publish PAYMENT_FAILURE event → notification-service
            PaymentEvent failEvent = new PaymentEvent(
                wallet.getWalletId(), wallet.getWalletId(),
                "PAYMENT_FAILURE", amount, wallet.getCurrentBalance(), orderId,
                "Insufficient balance"
            );
            eventPublisher.publishPaymentFailure(failEvent);
            throw new IllegalStateException("Insufficient balance");
        }

        wallet.setCurrentBalance(wallet.getCurrentBalance() - amount);
        Statement s = new Statement("WITHDRAW", amount, LocalDateTime.now(), orderId, remarks);
        wallet.getStatements().add(s);
        walletRepository.save(wallet);

        // Publish PAYMENT_SUCCESS event → notification-service
        PaymentEvent successEvent = new PaymentEvent(
            wallet.getWalletId(), wallet.getWalletId(),
            "PAYMENT_SUCCESS", amount, wallet.getCurrentBalance(), orderId, remarks
        );
        eventPublisher.publishPaymentSuccess(successEvent);
    }

    @Override
    public Wallet getById(int walletId) {
        Wallet w = walletRepository.findByWalletId(walletId);
        if (w == null) throw new RuntimeException("Wallet not found: " + walletId);
        return w;
    }

    @Override public List<Statement> getStatementsById(int walletId) { return statementsRepository.findByWalletId(walletId); }
    @Override public List<Statement> getStatements() { return statementsRepository.findAll(); }
    @Override public void deleteById(int walletId) { walletRepository.deleteByWalletId(walletId); }
}
