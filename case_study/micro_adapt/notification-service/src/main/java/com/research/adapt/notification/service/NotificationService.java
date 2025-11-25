package com.research.adapt.notification.service;
import com.research.adapt.events.order.OrderCreated;
import com.research.adapt.events.billing.InvoiceGenerated;
import com.research.adapt.notification.domain.Notification;
import com.research.adapt.notification.domain.NotificationStatus;
import com.research.adapt.notification.domain.NotificationType;
import com.research.adapt.notification.event.NotificationEventProducer;
import com.research.adapt.notification.repository.NotificationRepository;
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
    private final NotificationEventProducer eventProducer;

    @Transactional
    public void sendOrderConfirmationNotification(OrderCreated event) {
        log.info("Sending order confirmation notification for order {}", event.getOrderId());
        String recipient = "user" + event.getUserId() + "@example.com";
        String message = "Your order #" + event.getOrderId() + " has been confirmed!";

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .orderId(event.getOrderId())
                .type(NotificationType.EMAIL)
                .recipient(recipient)
                .subject("Order Confirmation")
                .message(message)
                .status(simulateEmailSend() ? NotificationStatus.SENT : NotificationStatus.FAILED)
                .sentAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        if (notification.getStatus() == NotificationStatus.SENT) {
            eventProducer.publishNotificationSent(event.getUserId(), event.getOrderId(), "EMAIL", recipient);
        }
    }

    @Transactional
    public void sendInvoiceNotification(InvoiceGenerated event) {
        log.info("Sending invoice notification for order {}", event.getOrderId());
        String recipient = "user" + event.getUserId() + "@example.com";
        String message = "Invoice " + event.getInvoiceNumber() + " for order #" + event.getOrderId() + " is ready.";

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .orderId(event.getOrderId())
                .type(NotificationType.EMAIL)
                .recipient(recipient)
                .subject("Invoice Ready - " + event.getInvoiceNumber())
                .message(message)
                .status(simulateEmailSend() ? NotificationStatus.SENT : NotificationStatus.FAILED)
                .sentAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        if (notification.getStatus() == NotificationStatus.SENT) {
            eventProducer.publishNotificationSent(event.getUserId(), event.getOrderId(), "EMAIL", recipient);
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    private boolean simulateEmailSend() {
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return true;
    }
}
