package com.example.identity.adapter.in.web;

import com.example.identity.domain.exception.EmailAlreadyExistsException;
import com.example.identity.domain.exception.InvalidCredentialsException;
import com.example.identity.domain.exception.UserNotFoundException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<Map<String, Object>> conflict(EmailAlreadyExistsException ex) {
    return body(HttpStatus.CONFLICT, "email_already_exists");
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Map<String, Object>> invalidCredentials(InvalidCredentialsException ex) {
    return body(HttpStatus.UNAUTHORIZED, "invalid_credentials");
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Map<String, Object>> userNotFound(UserNotFoundException ex) {
    return body(HttpStatus.UNAUTHORIZED, "invalid_credentials");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
    return body(HttpStatus.BAD_REQUEST, "validation_failed");
  }

  private static ResponseEntity<Map<String, Object>> body(HttpStatus status, String code) {
    return ResponseEntity.status(status)
        .body(
            Map.of("timestamp", Instant.now().toString(), "status", status.value(), "error", code));
  }
}
