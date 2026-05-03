package com.example.task.integration.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.task.adapter.out.persistence.TaskJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TaskControllerIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  static RSAPublicKey publicKey;
  static RSAPrivateKey privateKey;

  @DynamicPropertySource
  static void identityJwks(DynamicPropertyRegistry registry) throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair kp = gen.generateKeyPair();
    publicKey = (RSAPublicKey) kp.getPublic();
    privateKey = (RSAPrivateKey) kp.getPrivate();

    Base64.Encoder b64 = Base64.getUrlEncoder().withoutPadding();
    String n = b64.encodeToString(stripLeadingZero(publicKey.getModulus().toByteArray()));
    String e = b64.encodeToString(stripLeadingZero(publicKey.getPublicExponent().toByteArray()));
    String jwksJson =
        "{\"keys\":[{\"kty\":\"RSA\",\"use\":\"sig\",\"alg\":\"RS256\",\"kid\":\"test-kid\","
            + "\"n\":\""
            + n
            + "\",\"e\":\""
            + e
            + "\"}]}";

    Path tmp = Files.createTempFile("jwks-", ".json");
    Files.writeString(tmp, jwksJson);
    registry.add("identity.jwks-uri", () -> tmp.toUri().toString());
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper mapper;
  @Autowired private TaskJpaRepository jpaRepository;

  @BeforeEach
  void clean() {
    jpaRepository.deleteAll();
  }

  // ---------------- Authentication ----------------

  @Test
  void without_authorization_header_returns_401() throws Exception {
    mockMvc.perform(get("/tasks")).andExpect(status().isUnauthorized());
  }

  @Test
  void with_malformed_token_returns_401() throws Exception {
    mockMvc
        .perform(get("/tasks").header("Authorization", "Bearer not-a-jwt"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void with_token_signed_by_other_key_returns_401() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair other = gen.generateKeyPair();
    String foreign =
        JWT.create()
            .withSubject(UUID.randomUUID().toString())
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
            .sign(
                Algorithm.RSA256(
                    (RSAPublicKey) other.getPublic(), (RSAPrivateKey) other.getPrivate()));

    mockMvc
        .perform(get("/tasks").header("Authorization", "Bearer " + foreign))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void with_expired_token_returns_401() throws Exception {
    String expired =
        JWT.create()
            .withSubject(UUID.randomUUID().toString())
            .withIssuedAt(Date.from(Instant.now().minusSeconds(7200)))
            .withExpiresAt(Date.from(Instant.now().minusSeconds(60)))
            .sign(Algorithm.RSA256(publicKey, privateKey));

    mockMvc
        .perform(get("/tasks").header("Authorization", "Bearer " + expired))
        .andExpect(status().isUnauthorized());
  }

  // ---------------- Create ----------------

  @Test
  void create_task_returns_201_with_userId_from_token() throws Exception {
    UUID userId = UUID.randomUUID();
    String token = tokenFor(userId);

    mockMvc
        .perform(
            post("/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"T\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.userId").value(userId.toString()))
        .andExpect(jsonPath("$.title").value("T"))
        .andExpect(jsonPath("$.isCompleted").value(false));
  }

  @Test
  void create_task_persists_optional_fields() throws Exception {
    UUID userId = UUID.randomUUID();
    String token = tokenFor(userId);

    mockMvc
        .perform(
            post("/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"title\":\"Buy\",\"description\":\"Milk\",\"dueDate\":\"2026-05-01\","
                        + "\"isCompleted\":true}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.description").value("Milk"))
        .andExpect(jsonPath("$.dueDate").value("2026-05-01"))
        .andExpect(jsonPath("$.isCompleted").value(true));
  }

  @Test
  void create_task_with_missing_title_returns_400() throws Exception {
    String token = tokenFor(UUID.randomUUID());
    mockMvc
        .perform(
            post("/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void create_task_with_blank_title_returns_400() throws Exception {
    String token = tokenFor(UUID.randomUUID());
    mockMvc
        .perform(
            post("/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"   \"}"))
        .andExpect(status().isBadRequest());
  }

  // ---------------- List ----------------

  @Test
  void list_returns_only_authenticated_users_tasks() throws Exception {
    UUID alice = UUID.randomUUID();
    UUID bob = UUID.randomUUID();
    create(alice, "A1");
    create(alice, "A2");
    create(bob, "B1");

    mockMvc
        .perform(get("/tasks").header("Authorization", "Bearer " + tokenFor(alice)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[*].title", containsInAnyOrder("A1", "A2")));

    mockMvc
        .perform(get("/tasks").header("Authorization", "Bearer " + tokenFor(bob)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("B1"));
  }

  @Test
  void list_returns_empty_array_for_user_with_no_tasks() throws Exception {
    String token = tokenFor(UUID.randomUUID());
    mockMvc
        .perform(get("/tasks").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  // ---------------- Update ----------------

  @Test
  void update_with_ownership_returns_200_and_replaces_fields() throws Exception {
    UUID userId = UUID.randomUUID();
    String taskId = create(userId, "Old");
    mockMvc
        .perform(
            put("/tasks/" + taskId)
                .header("Authorization", "Bearer " + tokenFor(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"title\":\"New\",\"description\":\"d\",\"dueDate\":\"2026-06-15\","
                        + "\"isCompleted\":true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("New"))
        .andExpect(jsonPath("$.description").value("d"))
        .andExpect(jsonPath("$.dueDate").value("2026-06-15"))
        .andExpect(jsonPath("$.isCompleted").value(true));
  }

  @Test
  void update_unknown_id_returns_404() throws Exception {
    String token = tokenFor(UUID.randomUUID());
    mockMvc
        .perform(
            put("/tasks/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"X\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void update_id_owned_by_other_user_returns_404_not_403() throws Exception {
    UUID owner = UUID.randomUUID();
    UUID intruder = UUID.randomUUID();
    String taskId = create(owner, "Owners");

    mockMvc
        .perform(
            put("/tasks/" + taskId)
                .header("Authorization", "Bearer " + tokenFor(intruder))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"hijack\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void update_with_blank_title_returns_400() throws Exception {
    UUID userId = UUID.randomUUID();
    String taskId = create(userId, "T");
    mockMvc
        .perform(
            put("/tasks/" + taskId)
                .header("Authorization", "Bearer " + tokenFor(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"  \"}"))
        .andExpect(status().isBadRequest());
  }

  // ---------------- Delete ----------------

  @Test
  void delete_with_ownership_returns_204_and_removes_from_list() throws Exception {
    UUID userId = UUID.randomUUID();
    String taskId = create(userId, "T");

    mockMvc
        .perform(delete("/tasks/" + taskId).header("Authorization", "Bearer " + tokenFor(userId)))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get("/tasks").header("Authorization", "Bearer " + tokenFor(userId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void delete_unknown_id_returns_404() throws Exception {
    String token = tokenFor(UUID.randomUUID());
    mockMvc
        .perform(delete("/tasks/" + UUID.randomUUID()).header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_id_owned_by_other_user_returns_404_not_403() throws Exception {
    UUID owner = UUID.randomUUID();
    UUID intruder = UUID.randomUUID();
    String taskId = create(owner, "Owners");

    mockMvc
        .perform(delete("/tasks/" + taskId).header("Authorization", "Bearer " + tokenFor(intruder)))
        .andExpect(status().isNotFound());

    assertThat(jpaRepository.findById(UUID.fromString(taskId))).isPresent();
  }

  // ---------------- helpers ----------------

  private String tokenFor(UUID userId) {
    Instant now = Instant.now();
    return JWT.create()
        .withSubject(userId.toString())
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plusSeconds(3600)))
        .sign(Algorithm.RSA256(publicKey, privateKey));
  }

  private String create(UUID userId, String title) throws Exception {
    var res =
        mockMvc
            .perform(
                post("/tasks")
                    .header("Authorization", "Bearer " + tokenFor(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"" + title + "\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    return mapper.readTree(res.getResponse().getContentAsString()).get("id").asText();
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
