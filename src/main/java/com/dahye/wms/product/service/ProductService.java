package com.dahye.wms.product.service;

import com.dahye.wms.common.exception.ProductOutOfStockException;
import com.dahye.wms.order.dto.request.OrderRequest;
import com.dahye.wms.product.domain.Product;
import com.dahye.wms.product.domain.ProductStockLog;
import com.dahye.wms.product.repository.ProductRepository;
import com.dahye.wms.product.repository.ProductStockLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductStockLogRepository productStockLogRepository;

    @Transactional(readOnly = true)
    public List<Product> getLockProductByIdList(List<Long> productIdList) {
        return productRepository.findByIdInWithPessimisticWrite(productIdList);
    }

    @Transactional(readOnly = true)
    public void validateStock(Map<Long, Product> productMap, List<OrderRequest> orderRequestList) {
        for (OrderRequest orderRequest : orderRequestList) {
            Product product = productMap.get(orderRequest.getProductId());

            if (product == null) {
                throw new IllegalArgumentException("INVALID_PRODUCT_ID");
            }

            if (product.getStock() < orderRequest.getQuantity()) {
                throw new ProductOutOfStockException();
            }
        }
    }

    @Transactional
    public void updateProductStock(Map<Long, Product> productMap, List<OrderRequest> orderRequestList, long orderId) {
        for (OrderRequest orderRequest : orderRequestList) {
            Product product = productMap.get(orderRequest.getProductId());
            product.setStock(product.getStock() - orderRequest.getQuantity());
            createProductStockLog(product.getId(), orderRequest.getQuantity(), orderId);
        }
    }

    @Transactional
    public void createProductStockLog(long productId, long stock, long orderId) {
        ProductStockLog productStockLog = ProductStockLog.builder()
                .productId(productId)
                .orderId(orderId)
                .stock(stock)
                .build();

        productStockLogRepository.save(productStockLog);
    }
}
