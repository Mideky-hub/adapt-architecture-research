package com.research.adapt.inventory.event;

import com.research.adapt.events.inventory.InventoryReserved;
import com.research.adapt.events.inventory.InventoryFailed;
import com.research.adapt.events.inventory.InventoryFailureCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Event Producer for Inventory Service
 * Demonstrates ADAPT Principle: Asynchronous First Communication
 * Publishes domain events to Kafka topics
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${adapt.kafka.topics.inventory-reserved}")
    private String inventoryReservedTopic;

    @Value("${adapt.kafka.topics.inventory-failed}")
    private String inventoryFailedTopic;

    /**
     * Publish InventoryReserved event
     * Demonstrates: Transparency through Contracts (Avro schema)
     */
    public void publishInventoryReserved(Long orderId, Long userId, List<ReservedItem> items) {
        try {
            List<com.research.adapt.events.inventory.ReservedItem> avroItems = items.stream()
                    .map(item -> com.research.adapt.events.inventory.ReservedItem.newBuilder()
                            .setProductId(item.getProductId())
                            .setQuantity(item.getQuantity())
                            .build())
                    .toList();

            InventoryReserved event = InventoryReserved.newBuilder()
                    .setOrderId(orderId)
                    .setUserId(userId)
                    .setItems(avroItems)
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build();

            kafkaTemplate.send(inventoryReservedTopic, orderId.toString(), event);
            log.info("Published InventoryReserved event for order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to publish InventoryReserved event for order ID: {}", orderId, e);
            throw new RuntimeException("Failed to publish inventory reserved event", e);
        }
    }

    /**
     * Publish InventoryFailed event
     */
    public void publishInventoryFailed(Long orderId, Long userId, String reason, InventoryFailureCode failureCode) {
        try {
            InventoryFailed event = InventoryFailed.newBuilder()
                    .setOrderId(orderId)
                    .setUserId(userId)
                    .setReason(reason)
                    .setFailureCode(failureCode)
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build();

            kafkaTemplate.send(inventoryFailedTopic, orderId.toString(), event);
            log.info("Published InventoryFailed event for order ID: {}, reason: {}", orderId, reason);
        } catch (Exception e) {
            log.error("Failed to publish InventoryFailed event for order ID: {}", orderId, e);
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReservedItem {
        private Long productId;
        private Integer quantity;
    }
}
