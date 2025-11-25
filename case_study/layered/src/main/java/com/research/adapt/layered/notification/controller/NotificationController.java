package com.research.adapt.layered.notification.controller;

import com.research.adapt.layered.notification.entity.Notification;
import com.research.adapt.layered.notification.entity.NotificationType;
import com.research.adapt.layered.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Notification> sendNotification(
            @RequestParam Long userId,
            @RequestParam NotificationType type,
            @RequestParam String subject,
            @RequestParam String message) {
        log.info("REST request to send notification to user ID: {}", userId);
        Notification notification = notificationService.sendNotification(userId, type, subject, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        log.info("REST request to get notifications for user ID: {}", userId);
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable Long userId) {
        log.info("REST request to get unread notifications for user ID: {}", userId);
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/mark-read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        log.info("REST request to mark notification as read: {}", id);
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
