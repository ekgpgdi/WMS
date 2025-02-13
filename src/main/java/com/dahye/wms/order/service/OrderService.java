package com.dahye.wms.order.service;

import com.dahye.wms.customer.domain.Customer;
import com.dahye.wms.order.domain.Order;
import com.dahye.wms.order.domain.OrderItem;
import com.dahye.wms.order.domain.OrderStatus;
import com.dahye.wms.order.dto.request.OrderProductRequest;
import com.dahye.wms.order.dto.request.OrderRequest;
import com.dahye.wms.order.repository.OrderItemRepository;
import com.dahye.wms.order.repository.OrderRepository;
import com.dahye.wms.product.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public static String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // 앞 8자리만 사용
        return "ORD-" + datePart + "-" + uuid;
    }

    @Transactional
    public Order order(Customer customer, Map<Long, Product> productMap, OrderRequest orderRequest) {
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .orderStatus(OrderStatus.SUCCESS)
                .postcode(orderRequest.getPostcode())
                .address(orderRequest.getAddress())
                .totalAmount(0L)
                .build();

        orderRepository.save(order);

        List<OrderItem> orderItemList = new ArrayList<>();

        long totalAmount = 0L;
        for (OrderProductRequest orderProductRequest : orderRequest.getOrderProductList()) {
            Product product = productMap.get(orderProductRequest.getProductId());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(orderProductRequest.getQuantity())
                    .price(product.getPrice() * orderProductRequest.getQuantity())
                    .build();

            orderItemList.add(orderItem);
            totalAmount += orderItem.getPrice();
        }

        orderItemRepository.saveAll(orderItemList);
        order.setOrderItemList(orderItemList);
        order.setTotalAmount(totalAmount);

        orderRepository.save(order);
        return order;
    }

    @Transactional(readOnly = true)
    public Page<Order> getOrderListByCustomerId(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable);
    }
}
