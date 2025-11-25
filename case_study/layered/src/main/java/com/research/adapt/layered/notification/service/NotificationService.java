package com.research.adapt.layered.notification.service;

import com.research.adapt.layered.notification.entity.Notification;
import com.research.adapt.layered.notification.entity.NotificationStatus;
import com.research.adapt.layered.notification.entity.NotificationType;
import com.research.adapt.layered.notification.repository.NotificationRepository;
import com.research.adapt.layered.user.entity.User;
import com.research.adapt.layered.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification sendNotification(Long userId, NotificationType type, String subject, String message) {
        log.info("Sending notification to user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .subject(subject)
                .message(message)
                .status(NotificationStatus.PENDING)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // Simulate sending notification
        boolean sent = sendNotificationViaChannel(type, user, subject, message);

        if (sent) {
            savedNotification.setStatus(NotificationStatus.SENT);
            savedNotification.setSentAt(LocalDateTime.now());
            notificationRepository.save(savedNotification);
            log.info("Notification sent successfully to user ID: {}", userId);
        } else {
            savedNotification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(savedNotification);
            log.error("Failed to send notification to user ID: {}", userId);
        }

        return savedNotification;
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        log.info("Fetching notifications for user ID: {}", userId);
        return notificationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        log.info("Fetching unread notifications for user ID: {}", userId);
        return notificationRepository.findUnreadNotificationsByUserId(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        log.info("Marking notification as read: {}", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
        log.info("Notification marked as read: {}", notificationId);
    }

    private boolean sendNotificationViaChannel(NotificationType type, User user, String subject, String message) {
        // Simulate notification sending
        log.info("Simulating {} notification to {}: {}", type, user.getEmail(), subject);

        switch (type) {
            case EMAIL:
                return sendEmail(user.getEmail(), subject, message);
            case SMS:
                return sendSMS(user.getPhoneNumber(), message);
            case PUSH_NOTIFICATION:
                return sendPushNotification(user.getId(), subject, message);
            case IN_APP:
                return true; // In-app notifications are already saved in DB
            default:
                return false;
        }
    }

    private boolean sendEmail(String email, String subject, String message) {
        log.info("Sending email to: {} with subject: {}", email, subject);
        // Simulate email sending
        return true;
    }

    private boolean sendSMS(String phoneNumber, String message) {
        log.info("Sending SMS to: {}", phoneNumber);
        // Simulate SMS sending
        return true;
    }

    private boolean sendPushNotification(Long userId, String subject, String message) {
        log.info("Sending push notification to user ID: {}", userId);
        // Simulate push notification
        return true;
    }
}
