package com.example.identity.integration.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.identity.adapter.out.jwt.RsaKeyHolder;
import com.example.identity.adapter.out.persistence.UserJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void jwtProps(DynamicPropertyRegistry registry) {
    registry.add("jwt.expiration-ms", () -> "3600000");
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper mapper;
  @Autowired private UserJpaRepository jpaRepository;
  @Autowired private RsaKeyHolder keyHolder;

  @BeforeEach
  void clean() {
    jpaRepository.deleteAll();
  }

  // ----- Registration -----

  @Test
  void register_returns_201_with_user_response() throws Exception {
    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"alice@example.com\",\"password\":\"s3cret\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.email").value("alice@example.com"))
        .andExpect(jsonPath("$.createdAt").isNotEmpty());
  }

  @Test
  void register_duplicate_email_returns_409() throws Exception {
    register("dup@example.com", "pw");
    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"dup@example.com\",\"password\":\"pw\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void register_missing_password_returns_400() throws Exception {
    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"foo@example.com\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void register_malformed_email_returns_400() throws Exception {
    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"notanemail\",\"password\":\"pw\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void register_persists_bcrypt_hash_not_plaintext() throws Exception {
    register("bcrypt@example.com", "secretValue");

    var entity = jpaRepository.findByEmail("bcrypt@example.com").orElseThrow();
    assertThat(entity.getPasswordHash()).isNotEqualTo("secretValue");
    assertThat(entity.getPasswordHash()).startsWith("$2a$");
  }

  // ----- Login -----

  @Test
  void login_with_correct_credentials_returns_token_with_uuid_subject() throws Exception {
    String userId = register("login@example.com", "pw");

    MvcResult res =
        mockMvc
            .perform(
                post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"login@example.com\",\"password\":\"pw\"}"))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode json = mapper.readTree(res.getResponse().getContentAsString());
    String token = json.get("token").asText();
    assertThat(token).isNotBlank();

    var decoded =
        JWT.require(Algorithm.RSA256(keyHolder.publicKey(), keyHolder.privateKey()))
            .build()
            .verify(token);
    assertThat(decoded.getAlgorithm()).isEqualTo("RS256");
    assertThat(decoded.getSubject()).isEqualTo(userId);
    assertThat(decoded.getIssuedAt()).isNotNull();
    assertThat(decoded.getExpiresAt()).isNotNull();
    long delta =
        decoded.getExpiresAt().toInstant().getEpochSecond()
            - decoded.getIssuedAt().toInstant().getEpochSecond();
    assertThat(delta).isEqualTo(3600L);
  }

  @Test
  void login_unknown_email_returns_401() throws Exception {
    mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ghost@example.com\",\"password\":\"pw\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_wrong_password_returns_401() throws Exception {
    register("rightuser@example.com", "rightpw");
    mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"rightuser@example.com\",\"password\":\"wrongpw\"}"))
        .andExpect(status().isUnauthorized());
  }

  // ----- Get Current User -----

  @Test
  void me_with_valid_token_returns_user() throws Exception {
    String userId = register("me@example.com", "pw");
    String token = login("me@example.com", "pw");

    mockMvc
        .perform(get("/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.email").value("me@example.com"));
  }

  @Test
  void me_without_authorization_header_returns_401() throws Exception {
    mockMvc.perform(get("/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void me_with_malformed_token_returns_401() throws Exception {
    mockMvc
        .perform(get("/me").header("Authorization", "Bearer not-a-jwt"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void me_with_expired_token_returns_401() throws Exception {
    String userId = register("expired@example.com", "pw");
    Instant past = Instant.now().minusSeconds(120);
    String expired =
        JWT.create()
            .withKeyId(keyHolder.keyId())
            .withSubject(userId)
            .withIssuedAt(Date.from(past.minusSeconds(60)))
            .withExpiresAt(Date.from(past))
            .sign(Algorithm.RSA256(keyHolder.publicKey(), keyHolder.privateKey()));

    mockMvc
        .perform(get("/me").header("Authorization", "Bearer " + expired))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void me_with_token_signed_by_different_key_returns_401() throws Exception {
    String userId = register("foreign@example.com", "pw");

    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair other = gen.generateKeyPair();
    String foreign =
        JWT.create()
            .withKeyId(keyHolder.keyId())
            .withSubject(userId)
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
            .sign(
                Algorithm.RSA256(
                    (RSAPublicKey) other.getPublic(), (RSAPrivateKey) other.getPrivate()));

    mockMvc
        .perform(get("/me").header("Authorization", "Bearer " + foreign))
        .andExpect(status().isUnauthorized());
  }

  // ----- JWKS -----

  @Test
  void jwks_endpoint_returns_public_key_in_jwks_format() throws Exception {
    mockMvc
        .perform(get("/.well-known/jwks.json"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.keys").isArray())
        .andExpect(jsonPath("$.keys.length()").value(1))
        .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
        .andExpect(jsonPath("$.keys[0].use").value("sig"))
        .andExpect(jsonPath("$.keys[0].alg").value("RS256"))
        .andExpect(jsonPath("$.keys[0].kid").isNotEmpty())
        .andExpect(jsonPath("$.keys[0].n").isNotEmpty())
        .andExpect(jsonPath("$.keys[0].e").isNotEmpty());
  }

  // ----- helpers -----

  private String register(String email, String password) throws Exception {
    MvcResult res =
        mockMvc
            .perform(
                post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    return mapper.readTree(res.getResponse().getContentAsString()).get("id").asText();
  }

  private String login(String email, String password) throws Exception {
    MvcResult res =
        mockMvc
            .perform(
                post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
    return mapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
  }
}
