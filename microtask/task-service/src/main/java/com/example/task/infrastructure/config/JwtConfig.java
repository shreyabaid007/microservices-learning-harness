package com.example.task.infrastructure.config;

import com.example.task.adapter.in.web.JwtAuthFilter;
import com.example.task.adapter.out.jwt.JwksPublicKeyLoader;
import com.example.task.adapter.out.jwt.JwtVerifier;
import java.security.interfaces.RSAPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

  @Bean
  public RSAPublicKey rsaPublicKey(@Value("${identity.jwks-uri}") String jwksUri) {
    return JwksPublicKeyLoader.loadFirst(jwksUri);
  }

  @Bean
  public JwtVerifier jwtVerifier(RSAPublicKey rsaPublicKey) {
    return new JwtVerifier(rsaPublicKey);
  }

  @Bean
  public JwtAuthFilter jwtAuthFilter(JwtVerifier verifier) {
    return new JwtAuthFilter(verifier);
  }
}
