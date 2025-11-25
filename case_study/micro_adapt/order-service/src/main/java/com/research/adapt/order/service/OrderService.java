package com.research.adapt.order.service;

import com.research.adapt.order.domain.Order;
import com.research.adapt.order.domain.OrderItem;
import com.research.adapt.order.domain.OrderStatus;
import com.research.adapt.order.dto.*;
import com.research.adapt.order.event.OrderEventProducer;
import com.research.adapt.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order Service - Domain-Cohesive Design
 * Demonstrates ADAPT Principle: Each service owns its complete business capability
 * This service owns the entire order domain - from API to data to events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;

    /**
     * Create a new order
     * Demonstrates: Asynchronous First - publishes event instead of synchronous calls
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for user ID: {}", request.getUserId());

        // Calculate total
        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order entity
        Order order = Order.builder()
                .userId(request.getUserId())
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .build();

        // Add items
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .build();
            order.addItem(orderItem);
        }

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());

        // Publish event - let other services react
        eventProducer.publishOrderCreated(savedOrder);

        return mapToResponse(savedOrder);
    }

    /**
     * Confirm order after successful payment
     * Called by PaymentEventListener when PaymentCompleted event is received
     */
    @Transactional
    public void confirmOrder(Long orderId, Long paymentId) {
        log.info("Confirming order ID: {} with payment ID: {}", orderId, paymentId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        // Publish OrderConfirmed event for downstream services (billing, notification)
        eventProducer.publishOrderConfirmed(order, paymentId);
    }

    /**
     * Mark order as failed
     * Called when payment fails or inventory is unavailable
     */
    @Transactional
    public void failOrder(Long orderId, String reason) {
        log.info("Failing order ID: {} - Reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);

        // Publish OrderFailed event
        eventProducer.publishOrderFailed(
                orderId,
                order.getUserId(),
                reason,
                com.research.adapt.events.order.OrderFailureCode.PAYMENT_FAILED
        );
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.info("Fetching order by ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        log.info("Fetching orders for user ID: {}", userId);
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(items)
                .orderDate(order.getOrderDate())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
