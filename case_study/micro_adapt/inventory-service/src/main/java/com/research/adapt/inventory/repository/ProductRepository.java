package com.research.adapt.inventory.repository;

import com.research.adapt.inventory.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity - p.reservedQuantity > 0")
    List<Product> findAvailableProducts();
}
