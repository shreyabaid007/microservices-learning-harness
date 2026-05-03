package com.example.identity.adapter.out.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtVerifier {

  private final com.auth0.jwt.JWTVerifier delegate;

  public JwtVerifier(RsaKeyHolder keyHolder) {
    this.delegate =
        JWT.require(Algorithm.RSA256(keyHolder.publicKey(), keyHolder.privateKey())).build();
  }

  public DecodedJWT verify(String token) {
    return delegate.verify(token);
  }
}
