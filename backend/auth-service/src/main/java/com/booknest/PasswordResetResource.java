package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.security.SecureRandom;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class PasswordResetResource {

    @Autowired private UserRepository userRepository;
    @Autowired private OtpStore otpStore;
    @Autowired private EmailService emailService;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final SecureRandom random = new SecureRandom();

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Email required"));

        boolean exists = userRepository.findByEmail(email).isPresent();
        if (exists) {
            String otp = String.format("%06d", random.nextInt(1_000_000));
            otpStore.save(email, otp);
            try {
                emailService.sendOtpEmail(email, otp);
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Email send nahi ho saka: " + e.getMessage()));
            }
        }
        return ResponseEntity.ok(Map.of("message", "Agar email registered hai toh OTP bhej diya gaya."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp = req.get("otp");
        if (email == null || otp == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Email aur OTP required"));
        if (!otpStore.verify(email, otp))
            return ResponseEntity.badRequest().body(Map.of("error", "OTP galat ya expire ho gaya"));
        return ResponseEntity.ok(Map.of("message", "OTP verified"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp = req.get("otp");
        String newPass = req.get("newPassword");
        if (email == null || otp == null || newPass == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Sab fields required hain"));
        if (newPass.length() < 6)
            return ResponseEntity.badRequest().body(Map.of("error", "Password minimum 6 characters"));
        if (!otpStore.verify(email, otp))
            return ResponseEntity.badRequest().body(Map.of("error", "OTP galat ya expire ho gaya"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPass));
        userRepository.save(user);
        otpStore.remove(email);
        return ResponseEntity.ok(Map.of("message", "Password successfully reset ho gaya!"));
    }
}
