package com.example.task.adapter.out.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public final class JwksPublicKeyLoader {

  private JwksPublicKeyLoader() {}

  public static RSAPublicKey loadFirst(String jwksUri) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root;
      try (InputStream in = URI.create(jwksUri).toURL().openStream()) {
        root = mapper.readTree(in);
      }
      JsonNode keys = root.path("keys");
      if (!keys.isArray() || keys.isEmpty()) {
        throw new IllegalStateException("JWKS at " + jwksUri + " contained no keys");
      }
      JsonNode jwk = keys.get(0);
      Base64.Decoder b64 = Base64.getUrlDecoder();
      BigInteger n = new BigInteger(1, b64.decode(jwk.get("n").asText()));
      BigInteger e = new BigInteger(1, b64.decode(jwk.get("e").asText()));
      return (RSAPublicKey)
          KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(n, e));
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to load JWKS from " + jwksUri, ex);
    }
  }
}
