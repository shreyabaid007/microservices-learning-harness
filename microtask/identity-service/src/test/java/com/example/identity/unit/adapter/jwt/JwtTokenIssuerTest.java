package com.example.identity.unit.adapter.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.identity.adapter.out.jwt.JwtTokenIssuer;
import com.example.identity.adapter.out.jwt.RsaKeyHolder;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenIssuerTest {

  private RsaKeyHolder keyHolder;
  private JwtTokenIssuer issuer;
  private static final long EXPIRATION_MS = 3_600_000L;

  @BeforeEach
  void setUp() {
    keyHolder = RsaKeyHolder.generate();
    issuer = new JwtTokenIssuer(keyHolder, EXPIRATION_MS);
  }

  @Test
  void issues_rs256_token_with_sub_iat_and_exp_claims() {
    UUID userId = UUID.randomUUID();

    String token = issuer.issue(userId);

    assertThat(token).isNotBlank();
    DecodedJWT decoded =
        JWT.require(Algorithm.RSA256(keyHolder.publicKey(), keyHolder.privateKey()))
            .build()
            .verify(token);

    assertThat(decoded.getAlgorithm()).isEqualTo("RS256");
    assertThat(decoded.getSubject()).isEqualTo(userId.toString());
    assertThat(decoded.getIssuedAt()).isNotNull();
    assertThat(decoded.getExpiresAt()).isNotNull();
    long deltaSeconds =
        decoded.getExpiresAt().toInstant().getEpochSecond()
            - decoded.getIssuedAt().toInstant().getEpochSecond();
    assertThat(deltaSeconds).isEqualTo(EXPIRATION_MS / 1000);
  }

  @Test
  void includes_kid_header_matching_holder_key_id() {
    String token = issuer.issue(UUID.randomUUID());

    DecodedJWT decoded = JWT.decode(token);

    assertThat(decoded.getKeyId()).isEqualTo(keyHolder.keyId());
  }
}
