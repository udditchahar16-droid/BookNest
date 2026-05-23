package com.booknest;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int notificationId;
    private int userId;
    private String type, message;
    private boolean isRead;
    private LocalDateTime createdAt;
    public Notification() { this.createdAt = LocalDateTime.now(); }
    public Notification(int userId, String type, String message) { this(); this.userId=userId; this.type=type; this.message=message; }
    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int id) { this.notificationId = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
