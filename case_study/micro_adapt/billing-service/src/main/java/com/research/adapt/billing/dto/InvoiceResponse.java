package com.research.adapt.billing.dto;

import com.research.adapt.billing.domain.InvoiceStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private Long orderId;
    private Long userId;
    private Long paymentId;
    private BigDecimal amount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
}
