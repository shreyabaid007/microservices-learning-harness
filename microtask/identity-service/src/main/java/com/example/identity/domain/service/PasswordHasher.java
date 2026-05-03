package com.example.identity.domain.service;

public interface PasswordHasher {

  String hash(String rawPassword);

  boolean verify(String rawPassword, String hashedPassword);
}
