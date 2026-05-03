package com.example.identity.infrastructure.config;

import com.example.identity.adapter.in.web.JwtAuthFilter;
import com.example.identity.adapter.out.jwt.JwtTokenIssuer;
import com.example.identity.adapter.out.jwt.JwtVerifier;
import com.example.identity.adapter.out.jwt.RsaKeyHolder;
import com.example.identity.application.port.out.TokenIssuer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

  @Bean
  public RsaKeyHolder rsaKeyHolder() {
    return RsaKeyHolder.generate();
  }

  @Bean
  public TokenIssuer tokenIssuer(
      RsaKeyHolder keyHolder, @Value("${jwt.expiration-ms}") long expirationMs) {
    return new JwtTokenIssuer(keyHolder, expirationMs);
  }

  @Bean
  public JwtVerifier jwtVerifier(RsaKeyHolder keyHolder) {
    return new JwtVerifier(keyHolder);
  }

  @Bean
  public JwtAuthFilter jwtAuthFilter(JwtVerifier verifier) {
    return new JwtAuthFilter(verifier);
  }
}
