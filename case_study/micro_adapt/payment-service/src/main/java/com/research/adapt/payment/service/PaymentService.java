package com.research.adapt.payment.service;

import com.research.adapt.events.inventory.InventoryReserved;
import com.research.adapt.events.payment.PaymentFailureCode;
import com.research.adapt.payment.domain.Payment;
import com.research.adapt.payment.domain.PaymentMethod;
import com.research.adapt.payment.domain.PaymentStatus;
import com.research.adapt.payment.dto.PaymentResponse;
import com.research.adapt.payment.event.PaymentEventProducer;
import com.research.adapt.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer eventProducer;

    @Transactional
    public void processPaymentForOrder(InventoryReserved event) {
        log.info("Processing payment for order ID: {}", event.getOrderId());

        try {
            // Check if payment already exists
            if (paymentRepository.existsByOrderId(event.getOrderId())) {
                log.warn("Payment already exists for order {}", event.getOrderId());
                return;
            }

            // Calculate total amount from items
            BigDecimal totalAmount = event.getItems().stream()
                    .map(item -> BigDecimal.ZERO) // In real scenario, we'd fetch product prices
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // For demo, we'll use a default amount
            totalAmount = BigDecimal.valueOf(100.00);

            // Simulate payment processing
            boolean paymentSuccessful = simulatePaymentGateway(event.getOrderId(), totalAmount);

            Payment payment = Payment.builder()
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .amount(totalAmount)
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .status(paymentSuccessful ? PaymentStatus.COMPLETED : PaymentStatus.FAILED)
                    .transactionId(UUID.randomUUID().toString())
                    .paymentDate(paymentSuccessful ? LocalDateTime.now() : null)
                    .failureReason(paymentSuccessful ? null : "Payment gateway declined")
                    .build();

            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment saved with ID: {} and status: {}", savedPayment.getId(), savedPayment.getStatus());

            if (paymentSuccessful) {
                eventProducer.publishPaymentCompleted(
                        event.getOrderId(),
                        event.getUserId(),
                        savedPayment.getId(),
                        savedPayment.getTransactionId(),
                        totalAmount.toString()
                );
            } else {
                eventProducer.publishPaymentFailed(
                        event.getOrderId(),
                        event.getUserId(),
                        "Payment gateway declined transaction",
                        PaymentFailureCode.GATEWAY_ERROR
                );
            }

        } catch (Exception e) {
            log.error("Error processing payment for order {}", event.getOrderId(), e);
            eventProducer.publishPaymentFailed(
                    event.getOrderId(),
                    event.getUserId(),
                    "Payment processing error: " + e.getMessage(),
                    PaymentFailureCode.SYSTEM_ERROR
            );
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        log.info("Fetching payment for order ID: {}", orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        log.info("Fetching payment by ID: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        log.info("Fetching payments for user ID: {}", userId);
        return paymentRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private boolean simulatePaymentGateway(Long orderId, BigDecimal amount) {
        log.info("Simulating payment gateway for order {} with amount {}", orderId, amount);
        // In production, this would call an actual payment gateway API
        // For demo purposes, assume all payments succeed
        try {
            Thread.sleep(100); // Simulate network latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true; // Payment successful
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
