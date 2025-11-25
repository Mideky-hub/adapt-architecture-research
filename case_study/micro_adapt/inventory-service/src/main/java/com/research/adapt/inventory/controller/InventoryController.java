package com.research.adapt.inventory.controller;

import com.research.adapt.inventory.dto.AvailabilityCheckResponse;
import com.research.adapt.inventory.dto.ProductResponse;
import com.research.adapt.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Inventory Controller - REST API
 * Demonstrates ADAPT Principle: Domain-Cohesive Design
 * Provides synchronous API for querying inventory state
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Get product by ID
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        log.info("REST request to get product by ID: {}", id);
        ProductResponse response = inventoryService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all products
     */
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("REST request to get all products");
        List<ProductResponse> responses = inventoryService.getAllProducts();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get available products (with stock)
     */
    @GetMapping("/products/available")
    public ResponseEntity<List<ProductResponse>> getAvailableProducts() {
        log.info("REST request to get available products");
        List<ProductResponse> responses = inventoryService.getAvailableProducts();
        return ResponseEntity.ok(responses);
    }

    /**
     * Check product availability
     */
    @GetMapping("/products/{id}/availability")
    public ResponseEntity<AvailabilityCheckResponse> checkAvailability(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("REST request to check availability for product {} with quantity {}", id, quantity);
        AvailabilityCheckResponse response = inventoryService.checkAvailability(id, quantity);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Inventory Service is running");
    }
}
