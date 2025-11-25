package com.research.adapt.payment.event;

import com.research.adapt.events.payment.PaymentCompleted;
import com.research.adapt.events.payment.PaymentFailed;
import com.research.adapt.events.payment.PaymentFailureCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${adapt.kafka.topics.payment-completed}")
    private String paymentCompletedTopic;

    @Value("${adapt.kafka.topics.payment-failed}")
    private String paymentFailedTopic;

    public void publishPaymentCompleted(Long orderId, Long userId, Long paymentId, String transactionId, String amount) {
        try {
            PaymentCompleted event = PaymentCompleted.newBuilder()
                    .setOrderId(orderId)
                    .setUserId(userId)
                    .setPaymentId(paymentId)
                    .setTransactionId(transactionId)
                    .setAmount(amount)
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build();

            kafkaTemplate.send(paymentCompletedTopic, orderId.toString(), event);
            log.info("Published PaymentCompleted event for order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to publish PaymentCompleted event for order ID: {}", orderId, e);
        }
    }

    public void publishPaymentFailed(Long orderId, Long userId, String reason, PaymentFailureCode failureCode) {
        try {
            PaymentFailed event = PaymentFailed.newBuilder()
                    .setOrderId(orderId)
                    .setUserId(userId)
                    .setReason(reason)
                    .setFailureCode(failureCode)
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build();

            kafkaTemplate.send(paymentFailedTopic, orderId.toString(), event);
            log.info("Published PaymentFailed event for order ID: {}, reason: {}", orderId, reason);
        } catch (Exception e) {
            log.error("Failed to publish PaymentFailed event for order ID: {}", orderId, e);
        }
    }
}
