package com.booknest;
import java.util.List;
public interface NotificationService {
    void sendNotification(Notification notification);
    void markAsRead(int notificationId);
    void markAllRead(int userId);
    List<Notification> getByUser(int userId);
    int getUnreadCount(int userId);
    void deleteNotification(int id);
    void sendEmailAlert(String to, String subject, String body);
}
