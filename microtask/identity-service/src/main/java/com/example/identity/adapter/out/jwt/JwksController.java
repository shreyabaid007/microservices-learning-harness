package com.example.identity.adapter.out.jwt;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwksController {

  private final RsaKeyHolder keyHolder;

  public JwksController(RsaKeyHolder keyHolder) {
    this.keyHolder = keyHolder;
  }

  @GetMapping("/.well-known/jwks.json")
  public Map<String, Object> jwks() {
    Base64.Encoder b64 = Base64.getUrlEncoder().withoutPadding();
    String n =
        b64.encodeToString(stripLeadingZero(keyHolder.publicKey().getModulus().toByteArray()));
    String e =
        b64.encodeToString(
            stripLeadingZero(keyHolder.publicKey().getPublicExponent().toByteArray()));
    Map<String, Object> jwk =
        Map.of(
            "kty", "RSA", "use", "sig", "alg", "RS256", "kid", keyHolder.keyId(), "n", n, "e", e);
    return Map.of("keys", List.of(jwk));
  }

  private static byte[] stripLeadingZero(byte[] bytes) {
    if (bytes.length > 1 && bytes[0] == 0) {
      byte[] out = new byte[bytes.length - 1];
      System.arraycopy(bytes, 1, out, 0, out.length);
      return out;
    }
    return bytes;
  }
}
