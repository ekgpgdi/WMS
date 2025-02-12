package com.dahye.wms.common.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    public Claims parseToken(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String generateJwt(Long id, String email) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(email) // 토큰의 주제 (사용자 식별 정보)
                .claim("id", id)
                .setIssuedAt(new Date()) // 생성 시각
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 유효 시간
                .signWith(key) // 서명
                .compact();
    }
}
