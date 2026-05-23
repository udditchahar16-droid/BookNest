package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

/**
 * OrderEmailService — BookNest Order Service
 *
 * Direct SMTP email (RabbitMQ pe depend nahi).
 * Welcome email ki tarah seedha JavaMailSender use karta hai.
 *
 * Emails sent:
 *   1. Order Placed   (COD / Wallet / Razorpay)
 *   2. Order Status Changed (CONFIRMED, DISPATCHED, DELIVERED, CANCELLED)
 */
@Service
public class OrderEmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Order Placed Email
    // ─────────────────────────────────────────────────────────────────────────
    public void sendOrderPlacedEmail(String toEmail, String fullName,
                                     int orderId, double amount,
                                     String paymentMode, String bookTitle) {
        if (!canSend(toEmail)) return;

        String subject = "BookNest — Order Placed Successfully! 🛒";
        String name    = (fullName != null && !fullName.isBlank()) ? fullName : "Reader";

        String html = """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;
                        border:1px solid #e0e0e0;border-radius:10px;overflow:hidden;">
              <div style="background:#1a237e;padding:28px;text-align:center;">
                <h1 style="color:#fff;margin:0;font-size:26px;">📚 BookNest</h1>
                <p style="color:#c5cae9;margin:6px 0 0;font-size:14px;">Discover. Read. Belong.</p>
              </div>

              <div style="padding:36px;">
                <div style="text-align:center;font-size:48px;margin-bottom:16px;">🛒</div>
                <h2 style="color:#1a237e;margin-top:0;text-align:center;">
                  Order Placed Successfully!
                </h2>
                <p style="color:#555;font-size:15px;">Hi <strong>%s</strong>,</p>
                <p style="color:#555;font-size:15px;">
                  Your order has been placed successfully. Here are the details:
                </p>

                <div style="background:#e8eaf6;border-radius:10px;padding:20px;margin:20px 0;">
                  <table style="width:100%%;border-collapse:collapse;font-size:14px;">
                    <tr>
                      <td style="padding:6px 0;color:#555;">📦 Order ID</td>
                      <td style="padding:6px 0;color:#1a237e;font-weight:bold;text-align:right;">#%d</td>
                    </tr>
                    <tr>
                      <td style="padding:6px 0;color:#555;">📖 Book</td>
                      <td style="padding:6px 0;color:#333;text-align:right;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:6px 0;color:#555;">💳 Payment Mode</td>
                      <td style="padding:6px 0;color:#333;text-align:right;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:6px 0;color:#555;">💰 Amount Paid</td>
                      <td style="padding:6px 0;color:#2e7d32;font-weight:bold;text-align:right;">₹%.2f</td>
                    </tr>
                    <tr>
                      <td style="padding:6px 0;color:#555;">📋 Status</td>
                      <td style="padding:6px 0;text-align:right;">
                        <span style="background:#1a237e;color:#fff;padding:3px 10px;
                                     border-radius:12px;font-size:12px;">PLACED</span>
                      </td>
                    </tr>
                  </table>
                </div>

                <p style="color:#555;font-size:14px;">
                  We'll notify you as your order progresses. Thank you for shopping with BookNest! 📚
                </p>
              </div>

              <div style="background:#f5f5f5;padding:16px;text-align:center;">
                <p style="color:#aaa;font-size:12px;margin:0;">
                  © 2026 BookNest Platform. All rights reserved.
                </p>
              </div>
            </div>
            """.formatted(name, orderId, bookTitle != null ? bookTitle : "Book #" + orderId,
                          paymentMode, amount);

        sendHtmlEmail(toEmail, subject, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Order Status Changed Email
    // ─────────────────────────────────────────────────────────────────────────
    public void sendOrderStatusEmail(String toEmail, String fullName,
                                     int orderId, String newStatus, String bookTitle) {
        if (!canSend(toEmail)) return;

        String icon    = iconForStatus(newStatus);
        String color   = colorForStatus(newStatus);
        String subject = "BookNest — Order #" + orderId + " " + friendlyStatus(newStatus);
        String name    = (fullName != null && !fullName.isBlank()) ? fullName : "Reader";
        String msg     = messageForStatus(newStatus, orderId);

        String html = """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;
                        border:1px solid #e0e0e0;border-radius:10px;overflow:hidden;">
              <div style="background:#1a237e;padding:28px;text-align:center;">
                <h1 style="color:#fff;margin:0;font-size:26px;">📚 BookNest</h1>
                <p style="color:#c5cae9;margin:6px 0 0;font-size:14px;">Discover. Read. Belong.</p>
              </div>

              <div style="padding:36px;">
                <div style="text-align:center;font-size:48px;margin-bottom:16px;">%s</div>
                <h2 style="color:%s;margin-top:0;text-align:center;">
                  Order %s
                </h2>
                <p style="color:#555;font-size:15px;">Hi <strong>%s</strong>,</p>

                <div style="background:#f8f9fa;border-left:4px solid %s;
                            border-radius:4px;padding:18px 20px;margin:20px 0;
                            color:#333;font-size:15px;line-height:1.7;">
                  %s
                </div>

                <div style="background:#e8eaf6;border-radius:10px;padding:16px;margin:20px 0;">
                  <table style="width:100%%;border-collapse:collapse;font-size:14px;">
                    <tr>
                      <td style="padding:5px 0;color:#555;">📦 Order ID</td>
                      <td style="padding:5px 0;color:#1a237e;font-weight:bold;text-align:right;">#%d</td>
                    </tr>
                    <tr>
                      <td style="padding:5px 0;color:#555;">📖 Book</td>
                      <td style="padding:5px 0;color:#333;text-align:right;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:5px 0;color:#555;">📋 Current Status</td>
                      <td style="padding:5px 0;text-align:right;">
                        <span style="background:%s;color:#fff;padding:3px 10px;
                                     border-radius:12px;font-size:12px;">%s</span>
                      </td>
                    </tr>
                  </table>
                </div>

                <p style="color:#888;font-size:13px;text-align:center;">
                  This is an automated message from BookNest. Please do not reply.
                </p>
              </div>

              <div style="background:#f5f5f5;padding:16px;text-align:center;">
                <p style="color:#aaa;font-size:12px;margin:0;">
                  © 2026 BookNest Platform. All rights reserved.
                </p>
              </div>
            </div>
            """.formatted(
                icon, color, friendlyStatus(newStatus), name,
                color, msg,
                orderId, bookTitle != null ? bookTitle : "Book #" + orderId,
                color, newStatus
        );

        sendHtmlEmail(toEmail, subject, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Shared sender
    // ─────────────────────────────────────────────────────────────────────────
    private void sendHtmlEmail(String toEmail, String subject, String html) {
        if (mailSender == null) {
            System.out.println("[ORDER-EMAIL-SKIPPED] SMTP not configured. To: " + toEmail);
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
            System.out.println("[ORDER-EMAIL-SENT] To: " + toEmail + " | Subject: " + subject);
        } catch (Exception e) {
            System.err.println("[ORDER-EMAIL-FAILED] To: " + toEmail + " | Error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private boolean canSend(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            System.out.println("[ORDER-EMAIL-SKIPPED] Invalid address: " + email);
            return false;
        }
        return true;
    }

    private String iconForStatus(String status) {
        if (status == null) return "📬";
        return switch (status.toUpperCase()) {
            case "CONFIRMED"  -> "✅";
            case "DISPATCHED" -> "🚚";
            case "DELIVERED"  -> "📦";
            case "CANCELLED"  -> "❌";
            default           -> "📋";
        };
    }

    private String colorForStatus(String status) {
        if (status == null) return "#1a237e";
        return switch (status.toUpperCase()) {
            case "CONFIRMED"  -> "#1a237e";
            case "DISPATCHED" -> "#e65100";
            case "DELIVERED"  -> "#2e7d32";
            case "CANCELLED"  -> "#c62828";
            default           -> "#1a237e";
        };
    }

    private String friendlyStatus(String status) {
        if (status == null) return "Updated";
        return switch (status.toUpperCase()) {
            case "CONFIRMED"  -> "Confirmed";
            case "DISPATCHED" -> "Dispatched";
            case "DELIVERED"  -> "Delivered";
            case "CANCELLED"  -> "Cancelled";
            default           -> status;
        };
    }

    private String messageForStatus(String status, int orderId) {
        if (status == null) return "Your order #" + orderId + " has been updated.";
        return switch (status.toUpperCase()) {
            case "CONFIRMED"  -> "Great news! Your order <strong>#" + orderId + "</strong> has been confirmed and is being processed. We'll dispatch it soon!";
            case "DISPATCHED" -> "Your order <strong>#" + orderId + "</strong> is on its way! Our delivery partner has picked it up. Get ready to receive it!";
            case "DELIVERED"  -> "Your order <strong>#" + orderId + "</strong> has been delivered successfully. We hope you enjoy reading! 📚<br/><br/>Don't forget to leave a review.";
            case "CANCELLED"  -> "Your order <strong>#" + orderId + "</strong> has been cancelled. If you did not request this, please contact our support team.";
            default           -> "Your order <strong>#" + orderId + "</strong> status has been updated to <strong>" + status + "</strong>.";
        };
    }
}
