package com.dahye.wms.product.repository;

import com.dahye.wms.product.domain.ProductStockLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStockLogRepository extends JpaRepository<ProductStockLog, Long> {
}
