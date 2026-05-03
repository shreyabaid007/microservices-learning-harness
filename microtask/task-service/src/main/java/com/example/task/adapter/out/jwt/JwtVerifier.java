package com.example.task.adapter.out.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.security.interfaces.RSAPublicKey;

public class JwtVerifier {

  private final com.auth0.jwt.JWTVerifier delegate;

  public JwtVerifier(RSAPublicKey publicKey) {
    this.delegate = JWT.require(Algorithm.RSA256(publicKey, null)).build();
  }

  public DecodedJWT verify(String token) {
    return delegate.verify(token);
  }
}
