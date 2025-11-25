package com.research.adapt.layered.payment.repository;

import com.research.adapt.layered.payment.entity.Payment;
import com.research.adapt.layered.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.order.user.id = :userId")
    List<Payment> findPaymentsByUserId(Long userId);

    boolean existsByOrderId(Long orderId);
}
