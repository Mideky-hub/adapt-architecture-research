package com.research.adapt.billing.controller;

import com.research.adapt.billing.dto.InvoiceResponse;
import com.research.adapt.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        log.info("REST request to get invoice by ID: {}", id);
        InvoiceResponse response = billingService.getInvoiceById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<InvoiceResponse> getInvoiceByOrderId(@PathVariable Long orderId) {
        log.info("REST request to get invoice for order ID: {}", orderId);
        InvoiceResponse response = billingService.getInvoiceByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByUserId(@PathVariable Long userId) {
        log.info("REST request to get invoices for user ID: {}", userId);
        List<InvoiceResponse> responses = billingService.getInvoicesByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Billing Service is running");
    }
}
