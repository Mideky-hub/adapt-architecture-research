package com.research.adapt.layered.billing.controller;

import com.research.adapt.layered.billing.entity.Invoice;
import com.research.adapt.layered.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/invoices/generate/{orderId}")
    public ResponseEntity<Invoice> generateInvoice(@PathVariable Long orderId) {
        log.info("REST request to generate invoice for order ID: {}", orderId);
        Invoice invoice = billingService.generateInvoice(orderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        log.info("REST request to get invoice by ID: {}", id);
        Invoice invoice = billingService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/invoices/order/{orderId}")
    public ResponseEntity<Invoice> getInvoiceByOrderId(@PathVariable Long orderId) {
        log.info("REST request to get invoice for order ID: {}", orderId);
        Invoice invoice = billingService.getInvoiceByOrderId(orderId);
        return ResponseEntity.ok(invoice);
    }

    @PutMapping("/invoices/{id}/mark-paid")
    public ResponseEntity<Invoice> markInvoiceAsPaid(@PathVariable Long id) {
        log.info("REST request to mark invoice as paid: {}", id);
        Invoice invoice = billingService.markInvoiceAsPaid(id);
        return ResponseEntity.ok(invoice);
    }
}
