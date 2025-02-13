package com.dahye.wms.order.facade;

import com.dahye.wms.common.domain.ResponseCode;
import com.dahye.wms.customer.domain.Customer;
import com.dahye.wms.customer.service.CustomerService;
import com.dahye.wms.order.domain.Order;
import com.dahye.wms.order.domain.OrderItem;
import com.dahye.wms.order.dto.request.OrderProductRequest;
import com.dahye.wms.order.dto.request.OrderRequest;
import com.dahye.wms.order.dto.response.OrderListResponse;
import com.dahye.wms.order.dto.response.OrderProductResponse;
import com.dahye.wms.order.dto.response.OrderResponse;
import com.dahye.wms.order.service.ExcelService;
import com.dahye.wms.order.service.OrderService;
import com.dahye.wms.product.domain.Product;
import com.dahye.wms.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
    private final ExcelService excelService;

    @Transactional
    public Long order(Authentication authentication, OrderRequest orderRequest) {
        Long customerId = (Long) authentication.getDetails();
        Customer customer = customerService.get(customerId);

        if(orderRequest.getOrderProductList().size() == 0) {
            throw new IllegalArgumentException(ResponseCode.INVALID_ORDER_REQUEST.toString());
        }

        List<Long> productIdList = orderRequest.getOrderProductList().stream().map(OrderProductRequest::getProductId).toList();
        List<Product> productList = productService.getLockProductByIdList(productIdList);

        Map<Long, Product> productMap = new HashMap<>();
        for (Product product : productList) {
            productMap.put(product.getId(), product); // 상품 ID를 키로, 상품 객체를 값으로 넣음
        }

        productService.validateStock(productMap, orderRequest.getOrderProductList());

        Order order = orderService.order(customer, productMap, orderRequest);
        productService.updateProductStock(productMap, orderRequest.getOrderProductList(), order.getId());

        return order.getId();
    }

    @Transactional
    public Long orderByExcelFile(Authentication authentication, MultipartFile file) {
        OrderRequest orderRequest = excelService.parseFile(file);
        return order(authentication, orderRequest);
    }

    @Transactional(readOnly = true)
    public OrderProductResponse orderProductResponse(OrderItem orderItem) {
        return OrderProductResponse.builder()
                .product(productService.makeProductResponse(orderItem.getProduct()))
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();
    }

    @Transactional(readOnly = true)
    public OrderResponse makeOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .orderProductList(order.getOrderItemList().stream().map(this::orderProductResponse).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public OrderListResponse getList(int page, int size, Authentication authentication) {
        Long customerId = (Long) authentication.getDetails();
        Page<Order> orderList = orderService.getOrderListByCustomerId(customerId, PageRequest.of(page, size));

        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (Order order : orderList) {
            orderResponseList.add(makeOrderResponse(order));
        }

        return OrderListResponse.builder()
                .orderList(orderResponseList)
                .totalPages(orderList.getTotalPages())
                .totalElements(orderList.getTotalElements())
                .isLast(orderList.isLast())
                .build();
    }
}
