package com.dahye.wms.order.repository;

import com.dahye.wms.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, OrderItemRepositoryCustom {
    List<OrderItem> findByOrderId(Long id);
}
