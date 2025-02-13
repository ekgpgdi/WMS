package com.dahye.wms.customer.service;


import com.dahye.wms.common.domain.ResponseCode;
import com.dahye.wms.common.exception.ExistException;
import com.dahye.wms.common.exception.InvalidPasswordException;
import com.dahye.wms.common.exception.NotFoundException;
import com.dahye.wms.common.service.JwtService;
import com.dahye.wms.customer.domain.Customer;
import com.dahye.wms.customer.repository.CustomerRepository;
import com.dahye.wms.customer.dto.request.SignRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;
    private final RedisTemplate<String, String> certCodeRedisTemplate;
    private final RedisTemplate<String, Boolean> checkVerifyEmailRedisTemplate;

    @Transactional(readOnly = true)
    public String authenticate(String email, String password) {
        Customer customer =
                customerRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_CUSTOMER.toString()));

        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new InvalidPasswordException(ResponseCode.INVALID_PASSWORD.toString());
        }

        return jwtService.generateJwt(customer.getId(), customer.getEmail());
    }

    public void clearCertCodeRedisIfExists(String email) {
        // 코드가 존재하면 삭제
        certCodeRedisTemplate.delete(email);
    }

    public void clearVerifyEmailRedisIfExists(String email) {
        // 코드가 존재하면 삭제
        checkVerifyEmailRedisTemplate.delete(email);
    }

    public String generateVerificationCode(String email) {
        clearCertCodeRedisIfExists(email);

        String code = UUID.randomUUID().toString().substring(0, 6); // 6자리 인증번호 생성
        certCodeRedisTemplate.opsForValue().set(email, code, 5, TimeUnit.MINUTES);
        return code;
    }

    public boolean verifyCertCode(String email, String certCode) {
        final String verifyCertCode = certCodeRedisTemplate.opsForValue().get(email);
        checkVerifyEmailRedisTemplate.opsForValue().set(email, true, 5, TimeUnit.MINUTES);
        return StringUtils.equals(certCode, verifyCertCode);
    }

    @Transactional
    public String sign(SignRequest signRequest) {
        if (checkEmail(signRequest.getEmail())) {
            throw new ExistException(ResponseCode.EXIST_EMAIL.toString());
        }

        if (!checkVerifyEmailRedisTemplate.hasKey(signRequest.getEmail())
                || !checkVerifyEmailRedisTemplate.opsForValue().get(signRequest.getEmail())) {
            throw new IllegalArgumentException(ResponseCode.REQUIRED_EMAIL_VERIFIED.toString());
        }

        Customer customer =
                Customer.builder()
                        .email(signRequest.getEmail())
                        .name(signRequest.getName())
                        .password(passwordEncoder.encode(signRequest.getPassword()))
                        .build();

        customerRepository.save(customer);
        clearVerifyEmailRedisIfExists(signRequest.getEmail());

        return jwtService.generateJwt(customer.getId(), customer.getEmail());
    }

    @Transactional(readOnly = true)
    public boolean checkEmail(String email) {
        return customerRepository.existsByEmail(email);
    }
}
