package com.example.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TaskInputExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleInvalidArgument(
      IllegalArgumentException exception) {

    return ResponseEntity.badRequest().body(
        Map.of("error", String.valueOf(exception.getMessage())));
  }
}