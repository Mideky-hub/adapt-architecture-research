package com.research.adapt.inventory.event;

import com.research.adapt.events.order.OrderCreated;
import com.research.adapt.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Event Listener for Order Events
 * Demonstrates ADAPT Principle: Asynchronous First Communication
 * Reacts to order events to reserve inventory
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final InventoryService inventoryService;

    /**
     * Listen to OrderCreated events
     * Demonstrates: Event-driven choreography instead of orchestration
     */
    @KafkaListener(
            topics = "${adapt.kafka.topics.order-created}",
            groupId = "inventory-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderCreated event) {
        log.info("Received OrderCreated event for order ID: {}", event.getOrderId());
        try {
            inventoryService.reserveInventoryForOrder(event);
            log.info("Inventory processed for order {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process inventory for order {}", event.getOrderId(), e);
        }
    }
}
