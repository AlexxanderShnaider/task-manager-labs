package com.example.controller;

import java.util.Map;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AsyncTaskExceptionHandler {

  @ExceptionHandler(TaskRejectedException.class)
  public ResponseEntity<Map<String, String>> handleRejectedTask(
      TaskRejectedException exception) {

    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of(
            "error",
            "Async queue is full. Try again later."));
  }
}