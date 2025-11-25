package com.research.adapt.notification.event;
import com.research.adapt.events.notification.NotificationSent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${adapt.kafka.topics.notification-sent}") private String notificationSentTopic;

    public void publishNotificationSent(Long userId, Long orderId, String notificationType, String recipient) {
        try {
            NotificationSent event = NotificationSent.newBuilder()
                    .setUserId(userId)
                    .setOrderId(orderId)
                    .setNotificationType(notificationType)
                    .setRecipient(recipient)
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build();
            kafkaTemplate.send(notificationSentTopic, userId.toString(), event);
            log.info("Published NotificationSent event for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Failed to publish NotificationSent event for user ID: {}", userId, e);
        }
    }
}
