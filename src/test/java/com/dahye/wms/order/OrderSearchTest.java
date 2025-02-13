package com.dahye.wms.order;

import com.dahye.wms.customer.domain.Customer;
import com.dahye.wms.customer.dto.request.LoginRequest;
import com.dahye.wms.customer.repository.CustomerRepository;
import com.dahye.wms.order.domain.Order;
import com.dahye.wms.order.domain.OrderItem;
import com.dahye.wms.order.domain.OrderStatus;
import com.dahye.wms.order.repository.OrderRepository;
import com.dahye.wms.product.domain.Product;
import com.dahye.wms.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@DisplayName("주문 내역 조회 테스트")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class OrderSearchTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessToken;

    @BeforeEach
    public void setUp() throws Exception {
        Customer customer = Customer.builder()
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

        Product product = Product.builder()
                .name("test 상품1")
                .description("test 상품1 description")
                .price(3000L)
                .stock(4)
                .build();
        productRepository.save(product);
        Order order = Order.builder()
                .customer(customer)
                .orderNumber("ORD-20250206113517-0A58E542")
                .orderStatus(OrderStatus.SUCCESS)
                .totalAmount(6000L)
                .build();

        orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .price(6000L)
                .build();

        order.setOrderItemList(List.of(orderItem));
    }

    @DisplayName("주문 목록 조회 테스트")
    @Test
    public void testGetProductList() throws Exception {
        mockMvc.perform(get("/api/v1/order")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.orderList.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.totalPages").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.totalElements").value(1));
    }

    @AfterEach
    public void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll(); // 테스트 후 데이터 삭제
    }
}
