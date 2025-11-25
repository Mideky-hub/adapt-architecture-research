package com.research.adapt.layered.payment.service;

import com.research.adapt.layered.order.entity.Order;
import com.research.adapt.layered.order.repository.OrderRepository;
import com.research.adapt.layered.payment.dto.PaymentRequest;
import com.research.adapt.layered.payment.dto.PaymentResponse;
import com.research.adapt.layered.payment.entity.Payment;
import com.research.adapt.layered.payment.entity.PaymentStatus;
import com.research.adapt.layered.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order ID: {}", request.getOrderId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + request.getOrderId()));

        if (paymentRepository.existsByOrderId(request.getOrderId())) {
            throw new RuntimeException("Payment already exists for order ID: " + request.getOrderId());
        }

        // Simulate payment processing
        boolean paymentSuccessful = simulatePaymentProcessing(request);

        Payment payment = Payment.builder()
                .order(order)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(paymentSuccessful ? PaymentStatus.COMPLETED : PaymentStatus.FAILED)
                .transactionId(UUID.randomUUID().toString())
                .paymentDate(paymentSuccessful ? LocalDateTime.now() : null)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment processed with status: {} for order ID: {}", savedPayment.getStatus(), request.getOrderId());

        return mapToResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        log.info("Fetching payment for order ID: {}", orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order ID: " + orderId));
        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        log.info("Fetching payment by ID: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + id));
        return mapToResponse(payment);
    }

    private boolean simulatePaymentProcessing(PaymentRequest request) {
        // Simulate payment gateway processing
        // In production, this would call an actual payment gateway API
        log.info("Simulating payment processing for amount: {}", request.getAmount());
        return true; // Assume payment always succeeds for demo purposes
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
