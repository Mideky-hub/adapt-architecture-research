package com.research.adapt.billing.event;

import com.research.adapt.events.payment.PaymentCompleted;
import com.research.adapt.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final BillingService billingService;

    @KafkaListener(
            topics = "${adapt.kafka.topics.payment-completed}",
            groupId = "billing-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(PaymentCompleted event) {
        log.info("Received PaymentCompleted event for order ID: {}", event.getOrderId());
        try {
            billingService.generateInvoiceForOrder(event);
            log.info("Invoice generated for order {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to generate invoice for order {}", event.getOrderId(), e);
        }
    }
}
