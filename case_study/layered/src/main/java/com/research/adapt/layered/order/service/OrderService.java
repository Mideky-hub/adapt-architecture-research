package com.research.adapt.layered.order.service;

import com.research.adapt.layered.inventory.entity.Product;
import com.research.adapt.layered.inventory.repository.ProductRepository;
import com.research.adapt.layered.inventory.service.InventoryService;
import com.research.adapt.layered.order.dto.CreateOrderRequest;
import com.research.adapt.layered.order.dto.OrderItemRequest;
import com.research.adapt.layered.order.dto.OrderItemResponse;
import com.research.adapt.layered.order.dto.OrderResponse;
import com.research.adapt.layered.order.entity.Order;
import com.research.adapt.layered.order.entity.OrderItem;
import com.research.adapt.layered.order.entity.OrderStatus;
import com.research.adapt.layered.order.repository.OrderRepository;
import com.research.adapt.layered.payment.dto.PaymentRequest;
import com.research.adapt.layered.payment.dto.PaymentResponse;
import com.research.adapt.layered.payment.entity.PaymentMethod;
import com.research.adapt.layered.payment.service.PaymentService;
import com.research.adapt.layered.user.entity.User;
import com.research.adapt.layered.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for user ID: {}", request.getUserId());

        // Validate user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        // Check inventory availability for all items
        for (OrderItemRequest itemRequest : request.getItems()) {
            if (!inventoryService.checkAvailability(itemRequest.getProductId(), itemRequest.getQuantity())) {
                throw new RuntimeException("Insufficient stock for product ID: " + itemRequest.getProductId());
            }
        }

        // Create order
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        // Add order items and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemRequest.getProductId()));

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();

            order.addItem(orderItem);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Reserve stock for all items
        try {
            for (OrderItemRequest itemRequest : request.getItems()) {
                inventoryService.reserveStock(itemRequest.getProductId(), itemRequest.getQuantity());
            }
        } catch (Exception e) {
            log.error("Failed to reserve stock, rolling back order", e);
            throw new RuntimeException("Failed to reserve stock: " + e.getMessage());
        }

        // Process payment (synchronous call)
        try {
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .orderId(savedOrder.getId())
                    .amount(totalAmount)
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .build();

            PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);

            if ("COMPLETED".equals(paymentResponse.getStatus().name())) {
                savedOrder.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(savedOrder);
                log.info("Order created and confirmed with ID: {}", savedOrder.getId());
            } else {
                savedOrder.setStatus(OrderStatus.FAILED);
                orderRepository.save(savedOrder);
                // Release reserved stock
                for (OrderItemRequest itemRequest : request.getItems()) {
                    inventoryService.releaseStock(itemRequest.getProductId(), itemRequest.getQuantity());
                }
                throw new RuntimeException("Payment failed for order");
            }
        } catch (Exception e) {
            log.error("Failed to process payment, releasing stock", e);
            // Release reserved stock
            for (OrderItemRequest itemRequest : request.getItems()) {
                inventoryService.releaseStock(itemRequest.getProductId(), itemRequest.getQuantity());
            }
            savedOrder.setStatus(OrderStatus.FAILED);
            orderRepository.save(savedOrder);
            throw new RuntimeException("Failed to process payment: " + e.getMessage());
        }

        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.info("Fetching order by ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
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
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(items)
                .orderDate(order.getOrderDate())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
