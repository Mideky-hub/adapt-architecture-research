package com.research.adapt.inventory.repository;

import com.research.adapt.inventory.domain.InventoryReservation;
import com.research.adapt.inventory.domain.InventoryReservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    List<InventoryReservation> findByOrderId(Long orderId);

    List<InventoryReservation> findByOrderIdAndStatus(Long orderId, ReservationStatus status);

    Optional<InventoryReservation> findByOrderIdAndProductId(Long orderId, Long productId);
}
