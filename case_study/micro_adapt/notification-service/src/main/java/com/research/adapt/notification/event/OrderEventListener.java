package com.research.adapt.notification.event;
import com.research.adapt.events.order.OrderCreated;
import com.research.adapt.events.billing.InvoiceGenerated;
import com.research.adapt.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {
    private final NotificationService notificationService;

    @KafkaListener(topics = "${adapt.kafka.topics.order-created}", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderCreated(OrderCreated event) {
        log.info("Received OrderCreated event for order ID: {}", event.getOrderId());
        try {
            notificationService.sendOrderConfirmationNotification(event);
        } catch (Exception e) {
            log.error("Failed to send order confirmation notification", e);
        }
    }

    @KafkaListener(topics = "${adapt.kafka.topics.invoice-generated}", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleInvoiceGenerated(InvoiceGenerated event) {
        log.info("Received InvoiceGenerated event for order ID: {}", event.getOrderId());
        try {
            notificationService.sendInvoiceNotification(event);
        } catch (Exception e) {
            log.error("Failed to send invoice notification", e);
        }
    }
}
