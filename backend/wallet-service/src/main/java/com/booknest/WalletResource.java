package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*")
public class WalletResource {

    @Autowired private WalletService walletService;

    /** GET /api/wallet — all wallets (admin) */
    @GetMapping
    public List<Wallet> getAllWallets() { return walletService.getWallets(); }

    /** GET /api/wallet/{walletId} — get wallet with current balance (auto-creates if not found) */
    @GetMapping("/{walletId}")
    public ResponseEntity<Wallet> getById(@PathVariable int walletId) {
        try {
            return ResponseEntity.ok(walletService.getById(walletId));
        } catch (Exception e) {
            // Auto-create wallet for new users who don't have one yet
            Wallet newWallet = new Wallet();
            newWallet.setWalletId(walletId);
            newWallet.setCurrentBalance(0.0);
            return ResponseEntity.ok(walletService.addWallet(newWallet));
        }
    }

    /** POST /api/wallet — create wallet for a new user (walletId == userId) */
    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody Wallet wallet) {
        // Ignore if wallet already exists
        try {
            return ResponseEntity.ok(walletService.getById(wallet.getWalletId()));
        } catch (Exception e) {
            return ResponseEntity.ok(walletService.addWallet(wallet));
        }
    }

    /**
     * POST /api/wallet/{walletId}/add
     * Body: { "amount": 500.0, "remarks": "Top-up" }
     * Returns full Wallet so frontend can update balance display immediately.
     */
    @PostMapping("/{walletId}/add")
    public ResponseEntity<Wallet> addMoney(@PathVariable int walletId,
                                           @RequestBody Map<String, Object> req) {
        Wallet wallet = walletService.getById(walletId);
        double amount  = Double.parseDouble(req.get("amount").toString());
        String remarks = req.getOrDefault("remarks", "Top-up").toString();
        walletService.addMoney(wallet, amount, remarks);
        return ResponseEntity.ok(walletService.getById(walletId)); // fresh copy with updated balance
    }

    /**
     * POST /api/wallet/{walletId}/deduct
     * Body: { "amount": 299.0, "remarks": "Order payment", "orderId": 42 }
     * Returns full Wallet on success; 400 on insufficient balance.
     */
    @PostMapping("/{walletId}/deduct")
    public ResponseEntity<Wallet> deductMoney(@PathVariable int walletId,
                                              @RequestBody Map<String, Object> req) {
        Wallet wallet  = walletService.getById(walletId);
        double amount  = Double.parseDouble(req.get("amount").toString());
        String remarks = req.getOrDefault("remarks", "Purchase").toString();
        int orderId    = Integer.parseInt(req.getOrDefault("orderId", "0").toString());
        walletService.update(wallet, amount, remarks, orderId);
        return ResponseEntity.ok(walletService.getById(walletId));
    }

    /** GET /api/wallet/{walletId}/statements */
    @GetMapping("/{walletId}/statements")
    public List<Statement> getStatements(@PathVariable int walletId) {
        return walletService.getStatementsById(walletId);
    }

    /** DELETE /api/wallet/{walletId} */
    @DeleteMapping("/{walletId}")
    public ResponseEntity<Void> deleteWallet(@PathVariable int walletId) {
        walletService.deleteById(walletId);
        return ResponseEntity.noContent().build();
    }
}
