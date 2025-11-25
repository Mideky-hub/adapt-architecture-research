package com.research.adapt.layered.inventory.controller;

import com.research.adapt.layered.inventory.dto.ProductResponse;
import com.research.adapt.layered.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        log.info("REST request to get product by ID: {}", id);
        ProductResponse response = inventoryService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("REST request to get all products");
        List<ProductResponse> responses = inventoryService.getAllProducts();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/products/available")
    public ResponseEntity<List<ProductResponse>> getAvailableProducts() {
        log.info("REST request to get available products");
        List<ProductResponse> responses = inventoryService.getAvailableProducts();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/products/{id}/check-availability")
    public ResponseEntity<Boolean> checkAvailability(@PathVariable Long id, @RequestParam Integer quantity) {
        log.info("REST request to check availability for product ID: {} with quantity: {}", id, quantity);
        boolean available = inventoryService.checkAvailability(id, quantity);
        return ResponseEntity.ok(available);
    }
}
