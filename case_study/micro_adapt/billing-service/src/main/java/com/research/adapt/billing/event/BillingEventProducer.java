package com.research.adapt.billing.event;

import com.research.adapt.events.billing.InvoiceGenerated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${adapt.kafka.topics.invoice-generated}")
    private String invoiceGeneratedTopic;

    public void publishInvoiceGenerated(Long orderId, Long userId, Long invoiceId, String invoiceNumber, String totalAmount) {
        try {
            InvoiceGenerated event = InvoiceGenerated.newBuilder()
                    .setOrderId(orderId)
                    .setUserId(userId)
                    .setInvoiceId(invoiceId)
                    .setInvoiceNumber(invoiceNumber)
                    .setTotalAmount(totalAmount)
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build();

            kafkaTemplate.send(invoiceGeneratedTopic, orderId.toString(), event);
            log.info("Published InvoiceGenerated event for order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to publish InvoiceGenerated event for order ID: {}", orderId, e);
        }
    }
}
