package com.booknest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationResource {

    @Autowired private NotificationService notificationService;

    /** GET /api/notifications/user/{userId} */
    @GetMapping("/user/{userId}")
    public List<Notification> getByUser(@PathVariable int userId) {
        return notificationService.getByUser(userId);
    }

    /** GET /api/notifications/user/{userId}/unread-count */
    @GetMapping("/user/{userId}/unread-count")
    public int getUnreadCount(@PathVariable int userId) {
        return notificationService.getUnreadCount(userId);
    }

    /** PUT /api/notifications/{id}/read */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable int id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    /** PUT /api/notifications/user/{userId}/read-all */
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllRead(@PathVariable int userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/notifications/send
     * Called by Angular frontend after order placement, wallet top-up etc.
     * Body: { "userId": 1, "type": "ORDER_PLACED", "message": "Your order has been placed!" }
     */
    @PostMapping("/send")
    public ResponseEntity<Notification> sendNotification(@RequestBody Map<String, Object> req) {
        Notification n = new Notification();
        n.setUserId(Integer.parseInt(req.get("userId").toString()));
        n.setType(req.getOrDefault("type", "INFO").toString());
        n.setMessage(req.getOrDefault("message", "").toString());
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationService.sendNotification(n);
        return ResponseEntity.ok(n);
    }

    /** DELETE /api/notifications/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
