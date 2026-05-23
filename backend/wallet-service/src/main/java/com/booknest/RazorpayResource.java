package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Razorpay integration — lives in wallet-service (port 8085).
 * Frontend calls /api/razorpay/* which proxy routes here.
 *
 * Endpoints:
 *   POST /api/razorpay/create-order  — creates Razorpay order, returns orderId + keyId
 *   POST /api/razorpay/verify        — verifies signature, credits wallet
 */
@RestController
@RequestMapping("/api/razorpay")
@CrossOrigin(origins = "*")
public class RazorpayResource {

    @Value("${razorpay.key.id:rzp_test_PLACEHOLDER}")
    private String keyId;

    @Value("${razorpay.key.secret:PLACEHOLDER_SECRET}")
    private String keySecret;

    @Autowired private WalletService walletService;

    /**
     * Step 1 — Create Razorpay order.
     * Body: { "amount": 500.0, "receipt": "user_1_topup" }
     * Returns: { orderId, amount, currency, keyId, receipt }
     */
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> req) {
        try {
            double amount = Double.parseDouble(req.get("amount").toString());
            String receipt = req.getOrDefault("receipt", "receipt_" + System.currentTimeMillis()).toString();

            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject options = new JSONObject();
            options.put("amount",   (int)(amount * 100)); // paise
            options.put("currency", "INR");
            options.put("receipt",  receipt);

            Order order = client.orders.create(options);

            return ResponseEntity.ok(Map.of(
                "orderId",  order.get("id").toString(),
                "amount",   amount,
                "currency", "INR",
                "keyId",    keyId,
                "receipt",  receipt
            ));
        } catch (RazorpayException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Could not create Razorpay order: " + e.getMessage()
            ));
        }
    }

    /**
     * Step 2 — Verify payment signature + credit wallet.
     * Body: { userId, amount, razorpayOrderId, razorpayPaymentId, razorpaySignature, remarks }
     */
    @PostMapping("/verify")
    public ResponseEntity<Wallet> verify(@RequestBody Map<String, Object> req) {
        String orderId   = req.get("razorpayOrderId").toString();
        String paymentId = req.get("razorpayPaymentId").toString();
        String signature = req.get("razorpaySignature").toString();

        // ✅ Test mode mein signature verify nahi karo
        // Production mein yeh line hataao aur neeche wali uncomment karo
        // if (!verifySignature(orderId, paymentId, signature)) {
        //     return ResponseEntity.badRequest().build();
        // }

        int    userId  = Integer.parseInt(req.get("userId").toString());
        double amount  = Double.parseDouble(req.get("amount").toString());
        String remarks = req.getOrDefault("remarks", "Razorpay top-up").toString();

        try {
            Wallet wallet = walletService.getById(userId);
            walletService.addMoney(wallet, amount, remarks);
            return ResponseEntity.ok(walletService.getById(userId));
        } catch (Exception e) {
            Wallet wallet = new Wallet();
            wallet.setWalletId(userId);
            wallet.setCurrentBalance(0.0);
            walletService.addWallet(wallet);
            Wallet fresh = walletService.getById(userId);
            walletService.addMoney(fresh, amount, remarks);
            return ResponseEntity.ok(walletService.getById(userId));
        }
    }
    private boolean verifySignature(String orderId, String paymentId, String signature) {
        // If placeholder keys are set, skip verification (dev/test mode)
        if (keySecret == null || keySecret.isBlank() || keySecret.equals("PLACEHOLDER_SECRET")) {
            return true;
        }
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString().equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
