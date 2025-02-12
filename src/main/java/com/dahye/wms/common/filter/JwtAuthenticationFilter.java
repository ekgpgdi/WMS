package com.dahye.wms.common.filter;

import com.dahye.wms.common.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
    String token = request.getHeader("Authorization");

    if (token != null && token.startsWith("Bearer ")) {
      String jwt = token.substring(7);
      Claims claims = jwtService.parseToken(jwt);

      Long customerId = claims.get("id", Long.class); // id 추출
      UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                      claims.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

      // Custom Authentication 객체에 customerId 설정
      authentication.setDetails(customerId);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }
}
