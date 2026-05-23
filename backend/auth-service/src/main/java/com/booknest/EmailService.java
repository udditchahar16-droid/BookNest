package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

/**
 * EmailService — BookNest Auth Service
 *
 * Provides HTML email templates for:
 *   1. OTP (Password Reset)
 *   2. Welcome Email (Account Created)
 *
 * JavaMailSender required=false hone se app sirf log karta hai
 * agar SMTP configure nahi hai — crash nahi hota.
 */
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    // ────────────────────────────────────────────────────────────────
    // 1. OTP Email — Password Reset
    // ────────────────────────────────────────────────────────────────
    public void sendOtpEmail(String toEmail, String otp) {
        if (!canSend(toEmail)) return;
        String subject = "BookNest — Password Reset OTP";
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:500px;margin:auto;
                        border:1px solid #e0e0e0;border-radius:10px;overflow:hidden;">
              <div style="background:#1a237e;padding:28px;text-align:center;">
                <h1 style="color:#fff;margin:0;font-size:26px;">📚 BookNest</h1>
                <p style="color:#c5cae9;margin:6px 0 0;">Discover. Read. Belong.</p>
              </div>
              <div style="padding:36px;">
                <h2 style="color:#1a237e;margin-top:0;">Password Reset OTP</h2>
                <p style="color:#555;">We received a request to reset your password.
                   Use the OTP below to proceed:</p>
                <div style="background:#e8eaf6;border-radius:10px;padding:24px;
                            text-align:center;margin:24px 0;letter-spacing:10px;
                            font-size:36px;font-weight:bold;color:#1a237e;">
                  %s
                </div>
                <p style="color:#888;font-size:13px;">
                  ⏱ This OTP will expire in <strong>10 minutes</strong>.<br/>
                  If you did not request this, please ignore this email.
                </p>
              </div>
              <div style="background:#f5f5f5;padding:16px;text-align:center;">
                <p style="color:#aaa;font-size:12px;margin:0;">
                  © 2026 BookNest. All rights reserved.
                </p>
              </div>
            </div>
            """.formatted(otp);
        sendHtmlEmail(toEmail, subject, html);
    }

    // ────────────────────────────────────────────────────────────────
    // 2. Welcome Email — Account Created
    // ────────────────────────────────────────────────────────────────
    public void sendWelcomeEmail(String toEmail, String fullName) {
        if (!canSend(toEmail)) return;
        String subject = "Welcome to BookNest — Account Created! 📚";
        String name = (fullName != null && !fullName.isBlank()) ? fullName : "Reader";
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;
                        border:1px solid #e0e0e0;border-radius:10px;overflow:hidden;">
              <div style="background:#1a237e;padding:32px;text-align:center;">
                <h1 style="color:#fff;margin:0;font-size:28px;">📚 BookNest</h1>
                <p style="color:#c5cae9;margin:8px 0 0;font-size:15px;">
                  Discover. Read. Belong.
                </p>
              </div>
              <div style="padding:36px;">
                <h2 style="color:#1a237e;margin-top:0;">
                  Welcome, %s! 👋
                </h2>
                <p style="color:#555;font-size:15px;line-height:1.6;">
                  Thank you for creating your BookNest account. Your reading journey
                  starts now!
                </p>

                <div style="background:#e8eaf6;border-radius:10px;padding:20px;
                            margin:24px 0;">
                  <p style="margin:0 0 12px;color:#1a237e;font-weight:bold;font-size:14px;">
                    ✅ What you can do now:
                  </p>
                  <ul style="margin:0;padding-left:20px;color:#555;font-size:14px;
                             line-height:2;">
                    <li>Browse thousands of books by genre, author & keyword</li>
                    <li>Add books to your cart and wishlist</li>
                    <li>Place orders via Cash on Delivery or e-wallet</li>
                    <li>Write reviews and rate your favourite reads</li>
                    <li>Top up your wallet and track all transactions</li>
                  </ul>
                </div>

                <div style="text-align:center;margin:28px 0;">
                  <a href="http://localhost:4200"
                     style="background:#1a237e;color:#fff;text-decoration:none;
                            padding:14px 36px;border-radius:6px;font-size:15px;
                            font-weight:bold;display:inline-block;">
                    Start Exploring →
                  </a>
                </div>

                <p style="color:#888;font-size:13px;">
                  If you did not create this account, please ignore this email.
                </p>
              </div>
              <div style="background:#f5f5f5;padding:16px;text-align:center;">
                <p style="color:#aaa;font-size:12px;margin:0;">
                  © 2026 BookNest Platform. All rights reserved.
                </p>
              </div>
            </div>
            """.formatted(name);
        sendHtmlEmail(toEmail, subject, html);
    }

    // ────────────────────────────────────────────────────────────────
    // Shared sender
    // ────────────────────────────────────────────────────────────────
    private void sendHtmlEmail(String toEmail, String subject, String html) {
        if (mailSender == null) {
            System.out.println("[EMAIL-SKIPPED] SMTP not configured."
                + " To: " + toEmail + " | Subject: " + subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail.isBlank() ? "noreply@booknest.com" : fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("[EMAIL-SENT] To: " + toEmail + " | Subject: " + subject);
        } catch (Exception e) {
            System.err.println("[EMAIL-FAILED] To: " + toEmail
                + " | Error: " + e.getMessage());
        }
    }

    /** Placeholder/empty email address handle karo safely */
    private boolean canSend(String email) {
        if (email == null || email.isBlank()) {
            System.out.println("[EMAIL-SKIPPED] Null/empty address");
            return false;
        }
        if (!email.contains("@")) {
            System.out.println("[EMAIL-SKIPPED] Invalid address: " + email);
            return false;
        }
        return true;
    }
}
