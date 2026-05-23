package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

/**
 * NotificationServiceImpl — in-app + HTML email notifications
 *
 * Email bheja jata hai:
 *   - Order placed (COD / Wallet / Razorpay)
 *   - Order status change: CONFIRMED, DISPATCHED, DELIVERED, CANCELLED
 *   - Payment success / failure / top-up
 *
 * Triggered via:
 *   - RabbitMQ → NotificationEventConsumer → sendEmailAlert()
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired private NotificationRepository notificationRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    // ── In-App Notification ───────────────────────────────────────────────────
    @Override
    public void sendNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    @Override
    public void markAsRead(int id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    public void markAllRead(int userId) {
        List<Notification> list = notificationRepository.findByUserId(userId);
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
    }

    @Override
    public List<Notification> getByUser(int userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public int getUnreadCount(int userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Override
    public void deleteNotification(int id) {
        notificationRepository.deleteByNotificationId(id);
    }

    // ── HTML Email Alert via SMTP ─────────────────────────────────────────────
    @Override
    public void sendEmailAlert(String to, String subject, String body) {
        if (!canSend(to)) return;

        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            System.out.println("[EMAIL-SKIPPED] SMTP not configured."
                + " To: " + to + " | Subject: " + subject);
            return;
        }

        String htmlBody = buildHtmlEmail(subject, body);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("[BookNest] " + subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            System.out.println("[EMAIL-SENT] To: " + to + " | Subject: " + subject);
        } catch (Exception e) {
            System.err.println("[EMAIL-FAILED] To: " + to
                + " | Error: " + e.getMessage());
        }
    }

    // ── HTML Template ─────────────────────────────────────────────────────────
    private String buildHtmlEmail(String subject, String body) {
        // Emoji aur icon choose karo subject ke basis pe
        String icon = chooseIcon(subject);
        String color = chooseColor(subject);

        return """
            <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;
                        border:1px solid #e0e0e0;border-radius:10px;overflow:hidden;">
              <div style="background:#1a237e;padding:28px;text-align:center;">
                <h1 style="color:#fff;margin:0;font-size:26px;">📚 BookNest</h1>
                <p style="color:#c5cae9;margin:6px 0 0;font-size:14px;">
                  Discover. Read. Belong.
                </p>
              </div>

              <div style="padding:36px;">
                <div style="text-align:center;font-size:48px;margin-bottom:16px;">
                  %s
                </div>
                <h2 style="color:%s;margin-top:0;text-align:center;font-size:20px;">
                  %s
                </h2>

                <div style="background:#f8f9fa;border-left:4px solid %s;
                            border-radius:4px;padding:18px 20px;margin:20px 0;
                            color:#333;font-size:15px;line-height:1.7;">
                  %s
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
            """.formatted(icon, color, subject, color, body);
    }

    private String chooseIcon(String subject) {
        if (subject == null) return "📬";
        String s = subject.toLowerCase();
        if (s.contains("placed"))    return "🛒";
        if (s.contains("confirmed")) return "✅";
        if (s.contains("dispatch"))  return "🚚";
        if (s.contains("deliver"))   return "📦";
        if (s.contains("cancel"))    return "❌";
        if (s.contains("payment") || s.contains("wallet")) return "💳";
        if (s.contains("top") || s.contains("topup"))      return "💰";
        return "📬";
    }

    private String chooseColor(String subject) {
        if (subject == null) return "#1a237e";
        String s = subject.toLowerCase();
        if (s.contains("cancel") || s.contains("fail")) return "#c62828";
        if (s.contains("deliver") || s.contains("success") || s.contains("topup")) return "#2e7d32";
        if (s.contains("dispatch")) return "#e65100";
        return "#1a237e";
    }

    private boolean canSend(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            System.out.println("[EMAIL-SKIPPED] Invalid address: " + email);
            return false;
        }
        // Placeholder addresses skip karo
        if (email.startsWith("user-") || email.startsWith("wallet-")) {
            System.out.println("[EMAIL-SKIPPED] Placeholder: " + email);
            return false;
        }
        return true;
    }
}
