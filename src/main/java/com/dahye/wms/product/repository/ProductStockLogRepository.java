package com.dahye.wms.product.repository;

import com.dahye.wms.product.domain.ProductStockLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductStockLogRepository extends JpaRepository<ProductStockLog, Long> {
    Optional<ProductStockLog> findByProductId(Long id);
}
