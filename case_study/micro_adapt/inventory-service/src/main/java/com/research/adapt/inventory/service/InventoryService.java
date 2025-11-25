package com.research.adapt.inventory.service;

import com.research.adapt.events.inventory.InventoryFailureCode;
import com.research.adapt.events.order.OrderCreated;
import com.research.adapt.inventory.domain.InventoryReservation;
import com.research.adapt.inventory.domain.Product;
import com.research.adapt.inventory.dto.AvailabilityCheckResponse;
import com.research.adapt.inventory.dto.ProductResponse;
import com.research.adapt.inventory.event.InventoryEventProducer;
import com.research.adapt.inventory.repository.InventoryReservationRepository;
import com.research.adapt.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Inventory Service - Domain-Cohesive Design
 * Demonstrates ADAPT Principle: Each service owns its complete business capability
 * This service owns the entire inventory domain - from API to data to events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryEventProducer eventProducer;

    /**
     * Reserve inventory for an order
     * Demonstrates: Asynchronous First - triggered by event, publishes result event
     */
    @Transactional
    public void reserveInventoryForOrder(OrderCreated event) {
        log.info("Processing inventory reservation for order ID: {}", event.getOrderId());

        try {
            List<InventoryEventProducer.ReservedItem> reservedItems = new ArrayList<>();

            // Check availability and reserve each item
            for (com.research.adapt.events.order.OrderItem orderItem : event.getItems()) {
                Product product = productRepository.findById(orderItem.getProductId())
                        .orElseThrow(() -> new RuntimeException(
                                "Product not found: " + orderItem.getProductId()));

                // Check if enough stock is available
                if (!product.isAvailable(orderItem.getQuantity())) {
                    log.warn("Insufficient inventory for product {} in order {}",
                            orderItem.getProductId(), event.getOrderId());

                    // Publish failure event
                    eventProducer.publishInventoryFailed(
                            event.getOrderId(),
                            event.getUserId(),
                            "Insufficient stock for product: " + product.getName(),
                            InventoryFailureCode.INSUFFICIENT_STOCK
                    );
                    return;
                }

                // Reserve the stock
                product.setReservedQuantity(product.getReservedQuantity() + orderItem.getQuantity());
                productRepository.save(product);

                // Create reservation record
                InventoryReservation reservation = InventoryReservation.builder()
                        .orderId(event.getOrderId())
                        .productId(orderItem.getProductId())
                        .quantity(orderItem.getQuantity())
                        .status(InventoryReservation.ReservationStatus.RESERVED)
                        .build();
                reservationRepository.save(reservation);

                reservedItems.add(InventoryEventProducer.ReservedItem.builder()
                        .productId(orderItem.getProductId())
                        .quantity(orderItem.getQuantity())
                        .build());

                log.info("Reserved {} units of product {} for order {}",
                        orderItem.getQuantity(), orderItem.getProductId(), event.getOrderId());
            }

            // Publish success event
            eventProducer.publishInventoryReserved(event.getOrderId(), event.getUserId(), reservedItems);
            log.info("Successfully reserved inventory for order {}", event.getOrderId());

        } catch (Exception e) {
            log.error("Error reserving inventory for order {}", event.getOrderId(), e);
            eventProducer.publishInventoryFailed(
                    event.getOrderId(),
                    event.getUserId(),
                    "Error processing inventory: " + e.getMessage(),
                    InventoryFailureCode.SYSTEM_ERROR
            );
        }
    }

    /**
     * Check product availability
     */
    @Transactional(readOnly = true)
    public AvailabilityCheckResponse checkAvailability(Long productId, Integer quantity) {
        log.info("Checking availability for product {} with quantity {}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        return AvailabilityCheckResponse.builder()
                .productId(productId)
                .requestedQuantity(quantity)
                .availableQuantity(product.getAvailableQuantity())
                .available(product.isAvailable(quantity))
                .build();
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product by ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        return mapToResponse(product);
    }

    /**
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get available products (with stock)
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getAvailableProducts() {
        log.info("Fetching available products");
        return productRepository.findAvailableProducts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancel reservation (when order is cancelled)
     */
    @Transactional
    public void cancelReservation(Long orderId) {
        log.info("Cancelling reservation for order {}", orderId);

        List<InventoryReservation> reservations = reservationRepository.findByOrderIdAndStatus(
                orderId, InventoryReservation.ReservationStatus.RESERVED);

        for (InventoryReservation reservation : reservations) {
            Product product = productRepository.findById(reservation.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + reservation.getProductId()));

            // Release reserved stock
            product.setReservedQuantity(product.getReservedQuantity() - reservation.getQuantity());
            productRepository.save(product);

            // Update reservation status
            reservation.setStatus(InventoryReservation.ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);

            log.info("Released {} units of product {} for order {}",
                    reservation.getQuantity(), reservation.getProductId(), orderId);
        }
    }

    /**
     * Confirm reservation (when payment succeeds)
     */
    @Transactional
    public void confirmReservation(Long orderId) {
        log.info("Confirming reservation for order {}", orderId);

        List<InventoryReservation> reservations = reservationRepository.findByOrderIdAndStatus(
                orderId, InventoryReservation.ReservationStatus.RESERVED);

        for (InventoryReservation reservation : reservations) {
            Product product = productRepository.findById(reservation.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + reservation.getProductId()));

            // Deduct from actual stock
            product.setStockQuantity(product.getStockQuantity() - reservation.getQuantity());
            product.setReservedQuantity(product.getReservedQuantity() - reservation.getQuantity());
            productRepository.save(product);

            // Update reservation status
            reservation.setStatus(InventoryReservation.ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);

            log.info("Confirmed {} units of product {} for order {}",
                    reservation.getQuantity(), reservation.getProductId(), orderId);
        }
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .reservedQuantity(product.getReservedQuantity())
                .availableQuantity(product.getAvailableQuantity())
                .sku(product.getSku())
                .build();
    }
}
