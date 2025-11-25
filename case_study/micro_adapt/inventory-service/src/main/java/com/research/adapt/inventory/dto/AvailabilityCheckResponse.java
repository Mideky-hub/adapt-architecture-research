package com.research.adapt.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityCheckResponse {
    private Long productId;
    private Integer requestedQuantity;
    private Integer availableQuantity;
    private boolean available;
}
