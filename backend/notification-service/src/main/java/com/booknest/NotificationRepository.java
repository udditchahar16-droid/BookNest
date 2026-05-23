package com.booknest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserId(int userId);
    List<Notification> findByUserIdAndIsRead(int userId, boolean isRead);
    int countByUserIdAndIsRead(int userId, boolean isRead);
    List<Notification> findByType(String type);
    void deleteByNotificationId(int id);
}
