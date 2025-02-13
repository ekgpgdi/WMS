package com.dahye.wms.order;

import com.dahye.wms.customer.domain.Customer;
import com.dahye.wms.customer.dto.request.LoginRequest;
import com.dahye.wms.customer.repository.CustomerRepository;
import com.dahye.wms.order.domain.Order;
import com.dahye.wms.order.domain.OrderItem;
import com.dahye.wms.order.repository.OrderItemRepository;
import com.dahye.wms.order.repository.OrderRepository;
import com.dahye.wms.product.domain.Product;
import com.dahye.wms.product.domain.ProductStockLog;
import com.dahye.wms.product.repository.ProductRepository;
import com.dahye.wms.product.repository.ProductStockLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@DisplayName("엑셀 주문 테스트")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class ExcelOrderTest {
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
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessToken;
    private Customer customer;
    private Product product1;
    private Product product2;
    private Product product3;

    private MockMultipartFile createExcelFile() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Orders");

        Row firstRow = sheet.createRow(0);
        firstRow.createCell(0).setCellValue("우편번호");
        firstRow.createCell(1).setCellValue("04320");

        Row secondRow = sheet.createRow(1);
        secondRow.createCell(0).setCellValue("주소");
        secondRow.createCell(1).setCellValue("서울특별시 용산구 한강대로 405");

        Row emptyRow = sheet.createRow(2);

        Row headerRow = sheet.createRow(3);
        headerRow.createCell(0).setCellValue("상품 ID");
        headerRow.createCell(1).setCellValue("재고");

        Row dataRow1 = sheet.createRow(4);
        dataRow1.createCell(0).setCellValue(product1.getId());
        dataRow1.createCell(1).setCellValue(2);

        Row dataRow2 = sheet.createRow(5);
        dataRow2.createCell(0).setCellValue(product2.getId());
        dataRow2.createCell(1).setCellValue(3);

        Row dataRow3 = sheet.createRow(6);
        dataRow3.createCell(0).setCellValue(product3.getId());
        dataRow3.createCell(1).setCellValue(7);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // MIME 타입
                outputStream.toByteArray()
        );
    }

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
                .stock(10)
                .build();

        product2 = Product.builder()
                .name("test 상품2")
                .description("test 상품2 description")
                .price(1000L)
                .stock(3)
                .build();

        product3 = Product.builder()
                .name("test 상품3")
                .description("test 상품3 description")
                .price(11000L)
                .stock(7)
                .build();

        productRepository.saveAll(List.of(product1, product2, product3));
    }

    @DisplayName("주문 실패 - 상품 재고 부족")
    @Test
    public void FailOrderByProductStock() throws Exception {
        Product product = productRepository.findById(product3.getId()).orElseThrow();
        product.setStock(0);
        productRepository.saveAndFlush(product);

        MockMultipartFile excelFile = createExcelFile();

        mockMvc.perform(multipart("/api/v1/order/excel")
                        .file(excelFile)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PRODUCT_OUT_OF_STOCK"));
    }

    @DisplayName("주문 성공")
    @Test
    public void orderByPayApi() throws Exception {
        MockMultipartFile excelFile = createExcelFile();

        mockMvc.perform(multipart("/api/v1/order/excel")
                        .file(excelFile)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"));

        Page<Order> orderList = orderRepository.findByCustomerId(customer.getId(), PageRequest.of(0, 1, Sort.by(Sort.Order.desc("id"))));
        assertTrue(orderList.getTotalElements() >= 1, "주문이 생성되지 않았습니다.");

        Order order = orderList.getContent().get(0);
        assertEquals(order.getTotalAmount(), 86000, "거래 금액이 맞지 않습니다.");

        List<OrderItem> orderItemList = orderItemRepository.findByOrderId(order.getId());
        assertEquals(orderItemList.size(), 3, "주문된 상품 수량이 맞지 않습니다.");

        Product updatedProduct1 = productRepository.findById(product1.getId()).orElseThrow();
        assertEquals(updatedProduct1.getStock(), 8, "상품 수량이 맞지 않습니다.");

        Product updatedProduct2 = productRepository.findById(product2.getId()).orElseThrow();
        assertEquals(updatedProduct2.getStock(), 0, "상품 수량이 맞지 않습니다.");

        Product updatedProduct3 = productRepository.findById(product3.getId()).orElseThrow();
        assertEquals(updatedProduct3.getStock(), 0, "상품 수량이 맞지 않습니다.");

        Optional<ProductStockLog> productStockLog1 = productStockLogRepository.findByProductId(product1.getId());
        assertTrue(productStockLog1.isPresent(), "상품 재고 로그가 쌓이지 않았습니다.");
        assertEquals(productStockLog1.get().getStock(), 2, "상품 재고 로그 수량이 맞지 않습니다.");

        Optional<ProductStockLog> productStockLog2 = productStockLogRepository.findByProductId(product2.getId());
        assertTrue(productStockLog2.isPresent(), "상품 재고 로그가 쌓이지 않았습니다.");
        assertEquals(productStockLog2.get().getStock(), 3, "상품 재고 로그 수량이 맞지 않습니다.");

        Optional<ProductStockLog> productStockLog3 = productStockLogRepository.findByProductId(product3.getId());
        assertTrue(productStockLog3.isPresent(), "상품 재고 로그가 쌓이지 않았습니다.");
        assertEquals(productStockLog3.get().getStock(), 7, "상품 재고 로그 수량이 맞지 않습니다.");
    }

    @AfterEach
    public void tearDown() {
        orderRepository.deleteAll();
        productStockLogRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll(); // 테스트 후 데이터 삭제
    }
}
