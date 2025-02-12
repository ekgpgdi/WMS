package com.dahye.wms.auth;

import com.dahye.wms.common.service.JwtService;
import com.dahye.wms.customer.domain.Customer;
import com.dahye.wms.customer.repository.CustomerRepository;
import com.dahye.wms.customer.request.EmailRequest;
import com.dahye.wms.customer.request.EmailVerifyRequest;
import com.dahye.wms.customer.request.SignRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@DisplayName("회원가입")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class SignTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private final String duplicationEmail = "test@google.com";

    private final String signEmail = "dahae80912@google.com";

    @BeforeEach
    public void setUp() throws Exception {
        Customer customer = Customer.builder()
                .name("테스트 유저")
                .email(duplicationEmail)
                .password(passwordEncoder.encode("password"))
                .build();
        customerRepository.save(customer);

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail(signEmail);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/send-verification-code")
                .content(objectMapper.writeValueAsString(emailRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();

        String verificationCode = JsonPath.read(result.getResponse().getContentAsString(), "$.content");

        EmailVerifyRequest emailVerifyRequest = new EmailVerifyRequest();
        emailVerifyRequest.setEmail(signEmail);
        emailVerifyRequest.setCertCode(verificationCode);

        mockMvc.perform(post("/api/v1/auth/verify-code")
                .content(objectMapper.writeValueAsString(emailVerifyRequest))
                .contentType(MediaType.APPLICATION_JSON));
    }

    @DisplayName("회원 가입 실패 - 인증 안 된 이메일")
    @Test
    public void FailSignByNotVerifyEmail() throws Exception {
        SignRequest signRequest = new SignRequest();
        signRequest.setEmail("dahae809123@google.com");
        signRequest.setPassword("test");
        signRequest.setName("인증 안된 이메일 고객");

        mockMvc.perform(post("/api/v1/auth/sign")
                        .content(objectMapper.writeValueAsString(signRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("REQUIRED_EMAIL_VERIFIED"));
    }

    @DisplayName("회원 가입 실패 - 중복  이메일")
    @Test
    public void FailSignByExistEmail() throws Exception {
        SignRequest signRequest = new SignRequest();
        signRequest.setEmail(duplicationEmail);
        signRequest.setPassword("test");
        signRequest.setName("중복 이메일 고객");

        mockMvc.perform(post("/api/v1/auth/sign")
                        .content(objectMapper.writeValueAsString(signRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("EXIST_EMAIL"));
    }

    @DisplayName("회원 가입 성공")
    @Test
    public void SuccessSign() throws Exception {
        SignRequest signRequest = new SignRequest();
        signRequest.setEmail(signEmail);
        signRequest.setPassword("1234");
        signRequest.setName("가입 고객");

        mockMvc.perform(post("/api/v1/auth/sign")
                        .content(objectMapper.writeValueAsString(signRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isString())
                .andDo(result -> {
                    String token = JsonPath.read(result.getResponse().getContentAsString(), "$.content");
                    String subject = jwtService.parseToken(token).getSubject();
                    assertEquals(signEmail, subject);
                });
    }

    @AfterEach
    public void tearDown() {
        customerRepository.deleteAll(); // 테스트 후 데이터 삭제
    }
}
