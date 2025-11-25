package com.research.adapt.order.event;

import com.research.adapt.events.payment.PaymentCompleted;
import com.research.adapt.events.payment.PaymentFailed;
import com.research.adapt.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Event Listener for Payment Events
 * Demonstrates ADAPT Principle: Asynchronous First Communication
 * Reacts to payment events to update order status
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderService orderService;

    /**
     * Listen to PaymentCompleted events
     * Demonstrates: Event-driven choreography instead of orchestration
     */
    @KafkaListener(
            topics = "${adapt.kafka.topics.payment-completed}",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(PaymentCompleted event) {
        log.info("Received PaymentCompleted event for order ID: {}", event.getOrderId());
        try {
            orderService.confirmOrder(event.getOrderId(), event.getPaymentId());
            log.info("Order {} confirmed after successful payment", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to confirm order {} after payment", event.getOrderId(), e);
        }
    }

    /**
     * Listen to PaymentFailed events
     */
    @KafkaListener(
            topics = "${adapt.kafka.topics.payment-failed}",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(PaymentFailed event) {
        log.info("Received PaymentFailed event for order ID: {}", event.getOrderId());
        try {
            orderService.failOrder(event.getOrderId(), "Payment failed: " + event.getReason());
            log.info("Order {} marked as failed due to payment failure", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to mark order {} as failed", event.getOrderId(), e);
        }
    }
}
