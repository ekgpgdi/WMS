package com.dahye.wms.auth;

import com.dahye.wms.customer.domain.Customer;
import com.dahye.wms.customer.repository.CustomerRepository;
import com.dahye.wms.customer.dto.request.EmailRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@DisplayName("인증 코드 발송")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class SendVerificationCodeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    public void setUp() throws JsonProcessingException {
        Customer customer = Customer.builder()
                .name("테스트 유저")
                .email("test@google.com")
                .password(passwordEncoder.encode("password"))
                .build();
        customerRepository.save(customer);
    }

    @DisplayName("인증 코드 발송 성공")
    @Test
    public void successSendVerificationCode() throws Exception {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("dahae80912@google.com");

        mockMvc.perform(post("/api/v1/auth/send-verification-code")
                        .content(objectMapper.writeValueAsString(emailRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").value(Matchers.hasLength(6)))
                .andReturn();
    }

    @DisplayName("인증 코드 발송 실패 - 이미 존재하는 이메일")
    @Test
    public void failSendVerificationCode() throws Exception {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("test@google.com");

        mockMvc.perform(post("/api/v1/auth/send-verification-code")
                        .content(objectMapper.writeValueAsString(emailRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("EXIST_EMAIL"))
                .andReturn();
    }

    @AfterEach
    public void tearDown() {
        customerRepository.deleteAll(); // 테스트 후 데이터 삭제
    }
}
