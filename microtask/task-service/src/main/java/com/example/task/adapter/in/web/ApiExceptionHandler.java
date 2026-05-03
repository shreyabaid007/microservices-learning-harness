package com.example.task.adapter.in.web;

import com.example.task.domain.exception.TaskNotFoundException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(TaskNotFoundException.class)
  public ResponseEntity<Map<String, String>> notFound(TaskNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "task_not_found"));
  }
}
