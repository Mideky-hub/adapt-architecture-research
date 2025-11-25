package com.research.adapt.billing.repository;

import com.research.adapt.billing.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByOrderId(Long orderId);
    List<Invoice> findByUserId(Long userId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    boolean existsByOrderId(Long orderId);
}
