package com.example.identity.adapter.out.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.identity.application.port.out.TokenIssuer;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class JwtTokenIssuer implements TokenIssuer {

  private final RsaKeyHolder keyHolder;
  private final long expirationMs;

  public JwtTokenIssuer(RsaKeyHolder keyHolder, long expirationMs) {
    this.keyHolder = keyHolder;
    this.expirationMs = expirationMs;
  }

  @Override
  public String issue(UUID userId) {
    Instant now = Instant.now();
    return JWT.create()
        .withKeyId(keyHolder.keyId())
        .withSubject(userId.toString())
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plusMillis(expirationMs)))
        .sign(Algorithm.RSA256(keyHolder.publicKey(), keyHolder.privateKey()));
  }
}
