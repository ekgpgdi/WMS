package com.dahye.wms.product.repository;

import com.dahye.wms.product.domain.Product;

import java.util.List;

public interface ProductRepositoryCustom {
    List<Product> findByIdInWithPessimisticWrite(List<Long> productIdList);
}
