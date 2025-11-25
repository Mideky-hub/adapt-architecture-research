package com.research.adapt.order.event;

import com.research.adapt.events.order.OrderConfirmed;
import com.research.adapt.events.order.OrderCreated;
import com.research.adapt.events.order.OrderFailed;
import com.research.adapt.events.order.OrderFailureCode;
import com.research.adapt.order.domain.Order;
import com.research.adapt.order.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Event Producer for Order Service
 * Demonstrates ADAPT Principle: Asynchronous First Communication
 * Publishes domain events to Kafka topics
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${adapt.kafka.topics.order-created}")
    private String orderCreatedTopic;

    @Value("${adapt.kafka.topics.order-confirmed}")
    private String orderConfirmedTopic;

    @Value("${adapt.kafka.topics.order-failed}")
    private String orderFailedTopic;

    /**
     * Publish OrderCreated event
     * Demonstrates: Transparency through Contracts (Avro schema)
     */
    public void publishOrderCreated(Order order) {
        try {
            OrderCreated event = OrderCreated.newBuilder()
                    .setOrderId(order.getId())
                    .setUserId(order.getUserId())
                    .setItems(order.getItems().stream()
                            .map(this::mapToAvroOrderItem)
                            .collect(Collectors.toList()))
                    .setTotalAmount(order.getTotalAmount().toString())
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build();

            kafkaTemplate.send(orderCreatedTopic, order.getId().toString(), event);
            log.info("Published OrderCreated event for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to publish OrderCreated event for order ID: {}", order.getId(), e);
            throw new RuntimeException("Failed to publish order created event", e);
        }
    }

    /**
     * Publish OrderConfirmed event
     */
    public void publishOrderConfirmed(Order order, Long paymentId) {
        try {
            OrderConfirmed event = OrderConfirmed.newBuilder()
                    .setOrderId(order.getId())
                    .setUserId(order.getUserId())
                    .setTotalAmount(order.getTotalAmount().toString())
                    .setPaymentId(paymentId)
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build();

            kafkaTemplate.send(orderConfirmedTopic, order.getId().toString(), event);
            log.info("Published OrderConfirmed event for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to publish OrderConfirmed event for order ID: {}", order.getId(), e);
        }
    }

    /**
     * Publish OrderFailed event
     */
    public void publishOrderFailed(Long orderId, Long userId, String reason, OrderFailureCode failureCode) {
        try {
            OrderFailed event = OrderFailed.newBuilder()
                    .setOrderId(orderId)
                    .setUserId(userId)
                    .setReason(reason)
                    .setFailureCode(failureCode)
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build();

            kafkaTemplate.send(orderFailedTopic, orderId.toString(), event);
            log.info("Published OrderFailed event for order ID: {}, reason: {}", orderId, reason);
        } catch (Exception e) {
            log.error("Failed to publish OrderFailed event for order ID: {}", orderId, e);
        }
    }

    private com.research.adapt.events.order.OrderItem mapToAvroOrderItem(OrderItem item) {
        return com.research.adapt.events.order.OrderItem.newBuilder()
                .setProductId(item.getProductId())
                .setQuantity(item.getQuantity())
                .setUnitPrice(item.getUnitPrice().toString())
                .build();
    }
}
