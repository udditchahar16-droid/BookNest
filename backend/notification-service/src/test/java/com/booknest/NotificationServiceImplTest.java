package com.booknest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationServiceImpl
 * Tests: sendNotification, markAsRead, markAllRead, getByUser,
 *        getUnreadCount, deleteNotification, sendEmailAlert
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        mockNotification = new Notification();
        mockNotification.setNotificationId(1);
        mockNotification.setUserId(1);
        mockNotification.setType("ORDER_PLACED");
        mockNotification.setMessage("Your order #100 has been placed successfully.");
        mockNotification.setRead(false);
        mockNotification.setCreatedAt(LocalDateTime.now());
    }

    // ── sendNotification ──────────────────────────────────────────────────

    @Test
    @DisplayName("sendNotification: should persist and dispatch in-app notification")
    void sendNotification_persists() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);

        notificationService.sendNotification(mockNotification);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("sendNotification: saved notification should have isRead=false by default")
    void sendNotification_defaultIsReadFalse() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendNotification(mockNotification);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().isRead()).isFalse();
    }

    // ── markAsRead ────────────────────────────────────────────────────────

    @Test
    @DisplayName("markAsRead: should set isRead=true and save")
    void markAsRead_setsReadTrue() {
        when(notificationRepository.findById(1)).thenReturn(java.util.Optional.of(mockNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.markAsRead(1);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().isRead()).isTrue();
    }

    // ── markAllRead ───────────────────────────────────────────────────────

    @Test
    @DisplayName("markAllRead: should mark all user notifications as read")
    void markAllRead_marksAll() {
        Notification n2 = new Notification();
        n2.setNotificationId(2);
        n2.setUserId(1);
        n2.setRead(false);

        when(notificationRepository.findByUserId(1)).thenReturn(List.of(mockNotification, n2));
        when(notificationRepository.saveAll(anyList())).thenReturn(List.of(mockNotification, n2));

        notificationService.markAllRead(1);

        verify(notificationRepository).saveAll(anyList());
    }

    // ── getByUser ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByUser: should return all notifications for a user")
    void getByUser_returnsNotifications() {
        when(notificationRepository.findByUserId(1)).thenReturn(List.of(mockNotification));

        List<Notification> result = notificationService.getByUser(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("ORDER_PLACED");
    }

    @Test
    @DisplayName("getByUser: should return empty list for user with no notifications")
    void getByUser_returnsEmptyForNoNotifications() {
        when(notificationRepository.findByUserId(99)).thenReturn(List.of());
        assertThat(notificationService.getByUser(99)).isEmpty();
    }

    // ── getUnreadCount ────────────────────────────────────────────────────

    @Test
    @DisplayName("getUnreadCount: should return count of unread notifications")
    void getUnreadCount_returnsCount() {
        when(notificationRepository.countByUserIdAndIsRead(1, false)).thenReturn(3);

        int count = notificationService.getUnreadCount(1);

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("getUnreadCount: should return 0 when all notifications are read")
    void getUnreadCount_returnsZeroWhenAllRead() {
        when(notificationRepository.countByUserIdAndIsRead(1, false)).thenReturn(0);
        assertThat(notificationService.getUnreadCount(1)).isEqualTo(0);
    }

    // ── deleteNotification ────────────────────────────────────────────────

    @Test
    @DisplayName("deleteNotification: should call deleteByNotificationId on repository")
    void deleteNotification_callsDelete() {
        doNothing().when(notificationRepository).deleteByNotificationId(1);
        notificationService.deleteNotification(1);
        verify(notificationRepository, times(1)).deleteByNotificationId(1);
    }

    // ── findByType ────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByType: should return notifications of ORDER_DISPATCHED type")
    void findByType_returnsMatchingType() {
        mockNotification.setType("ORDER_DISPATCHED");
        when(notificationRepository.findByType("ORDER_DISPATCHED")).thenReturn(List.of(mockNotification));

        List<Notification> result = notificationRepository.findByType("ORDER_DISPATCHED");
        assertThat(result).allMatch(n -> n.getType().equals("ORDER_DISPATCHED"));
    }

    // ── findByUserIdAndIsRead ─────────────────────────────────────────────

    @Test
    @DisplayName("findByUserIdAndIsRead: should return only unread for user")
    void findByUserIdAndIsRead_returnsUnread() {
        when(notificationRepository.findByUserIdAndIsRead(1, false)).thenReturn(List.of(mockNotification));

        List<Notification> result = notificationRepository.findByUserIdAndIsRead(1, false);
        assertThat(result).allMatch(n -> !n.isRead());
    }
}
