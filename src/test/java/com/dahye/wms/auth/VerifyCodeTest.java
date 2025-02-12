package com.dahye.wms.auth;

import com.dahye.wms.customer.request.EmailRequest;
import com.dahye.wms.customer.request.EmailVerifyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@DisplayName("인증 코드 인증")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class VerifyCodeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String verificationCode;

    @BeforeEach
    public void setUp() throws Exception {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("dahae80912@google.com");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/send-verification-code")
                        .content(objectMapper.writeValueAsString(emailRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").value(Matchers.hasLength(6)))
                .andReturn();

        // 인증 코드 추출
        verificationCode = JsonPath.read(result.getResponse().getContentAsString(), "$.content");
    }

    @DisplayName("인증 코드 인증 실패")
    @Test
    public void FailVerifyCode() throws Exception {
        EmailVerifyRequest emailVerifyRequest = new EmailVerifyRequest();
        emailVerifyRequest.setEmail("dahae80912@google.com");
        emailVerifyRequest.setCertCode(verificationCode + "1");

        mockMvc.perform(post("/api/v1/auth/verify-code")
                        .content(objectMapper.writeValueAsString(emailVerifyRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("VERIFIED_CODE_ERROR"));
    }


    @DisplayName("인증 코드 인증 성공")
    @Test
    public void SuccessVerifyCode() throws Exception {
        EmailVerifyRequest emailVerifyRequest = new EmailVerifyRequest();
        emailVerifyRequest.setEmail("dahae80912@google.com");
        emailVerifyRequest.setCertCode(verificationCode);

        mockMvc.perform(post("/api/v1/auth/verify-code")
                        .content(objectMapper.writeValueAsString(emailVerifyRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"));
    }
}
