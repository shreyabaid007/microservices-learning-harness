package com.example.task.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateTaskRequest(
    @NotBlank String title, String description, LocalDate dueDate, Boolean isCompleted) {}
