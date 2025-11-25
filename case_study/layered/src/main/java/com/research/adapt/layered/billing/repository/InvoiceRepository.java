package com.research.adapt.layered.billing.repository;

import com.research.adapt.layered.billing.entity.Invoice;
import com.research.adapt.layered.billing.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByOrderId(Long orderId);

    List<Invoice> findByStatus(InvoiceStatus status);

    @Query("SELECT i FROM Invoice i WHERE i.order.user.id = :userId")
    List<Invoice> findInvoicesByUserId(Long userId);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :date AND i.status = :status")
    List<Invoice> findOverdueInvoices(LocalDateTime date, InvoiceStatus status);

    boolean existsByInvoiceNumber(String invoiceNumber);
}
