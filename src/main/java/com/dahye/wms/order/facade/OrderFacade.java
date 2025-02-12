package com.dahye.wms.order.facade;

import com.dahye.wms.common.domain.ResponseCode;
import com.dahye.wms.customer.domain.Customer;
import com.dahye.wms.customer.service.CustomerService;
import com.dahye.wms.order.domain.Order;
import com.dahye.wms.order.dto.request.OrderRequest;
import com.dahye.wms.order.service.OrderService;
import com.dahye.wms.product.domain.Product;
import com.dahye.wms.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacade {
    private final OrderService orderService;
    private final CustomerService customerService;
    private final ProductService productService;

    @Transactional
    public Long order(Authentication authentication, OrderRequest orderRequest) {
        Long customerId = (Long) authentication.getDetails();
        Customer customer = customerService.get(customerId);

        List<Long> productIdList = List.of(orderRequest.getProductId());
        List<Product> productList = productService.getLockProductByIdList(productIdList);

        Map<Long, Product> productMap = new HashMap<>();
        for (Product product : productList) {
            productMap.put(product.getId(), product); // 상품 ID를 키로, 상품 객체를 값으로 넣음
        }

        productService.validateStock(productMap, List.of(orderRequest));

        Order order = orderService.order(customer, productMap, List.of(orderRequest));
        productService.updateProductStock(productMap, List.of(orderRequest), order.getId());

        return order.getId();
    }
}
