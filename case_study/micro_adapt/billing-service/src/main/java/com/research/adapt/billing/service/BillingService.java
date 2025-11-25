package com.research.adapt.billing.service;

import com.research.adapt.events.payment.PaymentCompleted;
import com.research.adapt.billing.domain.Invoice;
import com.research.adapt.billing.domain.InvoiceStatus;
import com.research.adapt.billing.dto.InvoiceResponse;
import com.research.adapt.billing.event.BillingEventProducer;
import com.research.adapt.billing.repository.InvoiceRepository;
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
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final BillingEventProducer eventProducer;

    @Transactional
    public void generateInvoiceForOrder(PaymentCompleted event) {
        log.info("Generating invoice for order ID: {}", event.getOrderId());

        try {
            if (invoiceRepository.existsByOrderId(event.getOrderId())) {
                log.warn("Invoice already exists for order {}", event.getOrderId());
                return;
            }

            BigDecimal amount = new BigDecimal(event.getAmount());
            BigDecimal tax = amount.multiply(BigDecimal.valueOf(0.10)); // 10% tax

            Invoice invoice = Invoice.builder()
                    .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .paymentId(event.getPaymentId())
                    .amount(amount)
                    .tax(tax)
                    .status(InvoiceStatus.ISSUED)
                    .dueDate(LocalDateTime.now().plusDays(30))
                    .build();

            Invoice savedInvoice = invoiceRepository.save(invoice);
            log.info("Invoice generated with number: {}", savedInvoice.getInvoiceNumber());

            eventProducer.publishInvoiceGenerated(
                    event.getOrderId(),
                    event.getUserId(),
                    savedInvoice.getId(),
                    savedInvoice.getInvoiceNumber(),
                    savedInvoice.getTotalAmount().toString()
            );

        } catch (Exception e) {
            log.error("Error generating invoice for order {}", event.getOrderId(), e);
        }
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByOrderId(Long orderId) {
        log.info("Fetching invoice for order ID: {}", orderId);
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Invoice not found for order: " + orderId));
        return mapToResponse(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        log.info("Fetching invoice by ID: {}", id);
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + id));
        return mapToResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByUserId(Long userId) {
        log.info("Fetching invoices for user ID: {}", userId);
        return invoiceRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderId(invoice.getOrderId())
                .userId(invoice.getUserId())
                .paymentId(invoice.getPaymentId())
                .amount(invoice.getAmount())
                .tax(invoice.getTax())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}
