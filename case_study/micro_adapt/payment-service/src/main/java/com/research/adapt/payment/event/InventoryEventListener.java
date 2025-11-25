package com.research.adapt.payment.event;

import com.research.adapt.events.inventory.InventoryReserved;
import com.research.adapt.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "${adapt.kafka.topics.inventory-reserved}",
            groupId = "payment-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryReserved(InventoryReserved event) {
        log.info("Received InventoryReserved event for order ID: {}", event.getOrderId());
        try {
            paymentService.processPaymentForOrder(event);
            log.info("Payment processed for order {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process payment for order {}", event.getOrderId(), e);
        }
    }
}
