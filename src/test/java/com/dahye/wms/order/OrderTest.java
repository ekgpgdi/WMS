package com.dahye.wms.order;

import com.dahye.wms.customer.domain.Customer;
import com.dahye.wms.customer.repository.CustomerRepository;
import com.dahye.wms.customer.dto.request.LoginRequest;
import com.dahye.wms.order.domain.Order;
import com.dahye.wms.order.dto.request.OrderProductRequest;
import com.dahye.wms.order.dto.request.OrderRequest;
import com.dahye.wms.order.repository.OrderRepository;
import com.dahye.wms.product.domain.Product;
import com.dahye.wms.product.domain.ProductStockLog;
import com.dahye.wms.product.repository.ProductRepository;
import com.dahye.wms.product.repository.ProductStockLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@DisplayName("주문 테스트")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class OrderTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStockLogRepository productStockLogRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessToken;
    private Customer customer;
    private Product product1;

    @BeforeEach
    public void setUp() throws Exception {
        customer = Customer.builder()
                .name("테스트 유저")
                .email("test@google.com")
                .password(passwordEncoder.encode("password"))
                .build();
        customerRepository.save(customer);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@google.com");
        loginRequest.setPassword("password");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .content(objectMapper.writeValueAsString(loginRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();

        // 인증 코드 추출
        accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.content");

        product1 = Product.builder()
                .name("test 상품1")
                .description("test 상품1 description")
                .price(3000L)
                .stock(1)
                .build();
        productRepository.save(product1);
    }

    @DisplayName("주문 실패 - 상품 재고 부족")
    @Test
    public void FailOrderByProductStock() throws Exception {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setPostcode("04320");
        orderRequest.setAddress("서울특별시 용산구 한강대로 405");

        OrderProductRequest orderProductRequest = new OrderProductRequest();
        orderProductRequest.setProductId(product1.getId());
        orderProductRequest.setQuantity(3);
        orderRequest.setOrderProductList(List.of(orderProductRequest));

        mockMvc.perform(post("/api/v1/order")
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(orderRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PRODUCT_OUT_OF_STOCK"));
    }

    @DisplayName("주문 성공")
    @Test
    public void orderByPayApi() throws Exception {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setPostcode("04320");
        orderRequest.setAddress("서울특별시 용산구 한강대로 405");

        OrderProductRequest orderProductRequest = new OrderProductRequest();
        orderProductRequest.setProductId(product1.getId());
        orderProductRequest.setQuantity(1);
        orderRequest.setOrderProductList(List.of(orderProductRequest));

        mockMvc.perform(post("/api/v1/order")
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(orderRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"));

        Page<Order> orderList = orderRepository.findByCustomerId(customer.getId(), PageRequest.of(0, 1, Sort.by(Sort.Order.desc("id"))));
        assertTrue(orderList.getTotalElements() >= 1, "주문이 생성되지 않았습니다.");

        Order order = orderList.getContent().get(0);
        assertEquals(order.getTotalAmount(), 3000L, "거래 금액이 맞지 않습니다.");

        Product updatedProduct = productRepository.findById(product1.getId()).orElseThrow();
        assertEquals(updatedProduct.getStock(), 0, "상품 수량이 맞지 않습니다.");

        Optional<ProductStockLog> payLog = productStockLogRepository.findByProductId(product1.getId());
        assertTrue(payLog.isPresent(), "상품 재고 로그가 쌓이지 않았습니다.");

    }

    @AfterEach
    public void tearDown() {
        orderRepository.deleteAll();
        productStockLogRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll(); // 테스트 후 데이터 삭제
    }
}
