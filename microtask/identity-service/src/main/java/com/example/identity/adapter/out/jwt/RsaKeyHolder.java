package com.example.identity.adapter.out.jwt;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

public final class RsaKeyHolder {

  private final RSAPublicKey publicKey;
  private final RSAPrivateKey privateKey;
  private final String keyId;

  public RsaKeyHolder(RSAPublicKey publicKey, RSAPrivateKey privateKey, String keyId) {
    this.publicKey = publicKey;
    this.privateKey = privateKey;
    this.keyId = keyId;
  }

  public static RsaKeyHolder generate() {
    try {
      KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
      gen.initialize(2048);
      KeyPair kp = gen.generateKeyPair();
      return new RsaKeyHolder(
          (RSAPublicKey) kp.getPublic(),
          (RSAPrivateKey) kp.getPrivate(),
          UUID.randomUUID().toString());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("RSA key pair generation failed", e);
    }
  }

  public RSAPublicKey publicKey() {
    return publicKey;
  }

  public RSAPrivateKey privateKey() {
    return privateKey;
  }

  public String keyId() {
    return keyId;
  }
}
