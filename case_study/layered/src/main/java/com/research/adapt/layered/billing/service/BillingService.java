package com.research.adapt.layered.billing.service;

import com.research.adapt.layered.billing.entity.Invoice;
import com.research.adapt.layered.billing.entity.InvoiceStatus;
import com.research.adapt.layered.billing.repository.InvoiceRepository;
import com.research.adapt.layered.order.entity.Order;
import com.research.adapt.layered.order.repository.OrderRepository;
import com.research.adapt.layered.payment.entity.Payment;
import com.research.adapt.layered.payment.repository.PaymentRepository;
import com.research.adapt.layered.user.entity.User;
import com.research.adapt.layered.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository; // ANTI-PATTERN: Direct database access across domains

    @Transactional
    public Invoice generateInvoice(Long orderId) {
        log.info("Generating invoice for order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // ANTI-PATTERN: Directly accessing User repository instead of going through UserService
        User user = userRepository.findById(order.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElse(null);

        // Calculate tax (10% for demonstration)
        BigDecimal tax = order.getTotalAmount().multiply(BigDecimal.valueOf(0.10));

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .order(order)
                .payment(payment)
                .amount(order.getTotalAmount())
                .tax(tax)
                .status(payment != null ? InvoiceStatus.ISSUED : InvoiceStatus.DRAFT)
                .dueDate(LocalDateTime.now().plusDays(30))
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice generated with number: {}", savedInvoice.getInvoiceNumber());

        return savedInvoice;
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceByOrderId(Long orderId) {
        log.info("Fetching invoice for order ID: {}", orderId);
        return invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Invoice not found for order ID: " + orderId));
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceById(Long id) {
        log.info("Fetching invoice by ID: {}", id);
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + id));
    }

    @Transactional
    public Invoice markInvoiceAsPaid(Long invoiceId) {
        log.info("Marking invoice as paid: {}", invoiceId);
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));

        invoice.setStatus(InvoiceStatus.PAID);
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice marked as paid: {}", updatedInvoice.getInvoiceNumber());

        return updatedInvoice;
    }
}
