package com.booknest;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpStore {

    private static final int OTP_VALIDITY_MINUTES = 10;

    private static class OtpEntry {
        final String code;
        final LocalDateTime expiresAt;
        OtpEntry(String code) {
            this.code = code;
            this.expiresAt = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);
        }
        boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
    }

    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    public void save(String email, String otp) {
        store.put(email.toLowerCase(), new OtpEntry(otp));
    }

    public boolean verify(String email, String otp) {
        OtpEntry entry = store.get(email.toLowerCase());
        if (entry == null || entry.isExpired()) return false;
        return entry.code.equals(otp);
    }

    public void remove(String email) {
        store.remove(email.toLowerCase());
    }
}
