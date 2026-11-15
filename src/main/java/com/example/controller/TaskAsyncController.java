package com.example.controller;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.example.service.TaskAsyncService;
import com.example.service.TaskAsyncOverloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks/async")
@RequiredArgsConstructor
public class TaskAsyncController {

  private final TaskAsyncService asyncService;
  private final TaskAsyncOverloadService overloadService;

  @PostMapping("/notify/{id}")
  public ResponseEntity<Map<String, String>> notifyTask(
      @PathVariable Long id,
      @RequestParam(defaultValue = "false") boolean fail) {

    asyncService.notifyTask(id, fail);

    return ResponseEntity.accepted().body(
        Map.of("message", "Notification scheduled"));
  }

  @GetMapping("/summary/{id}")
  public CompletableFuture<ResponseEntity<Map<String, String>>> getSummary(
      @PathVariable Long id,
      @RequestParam(defaultValue = "false") boolean fail,
      @RequestParam(defaultValue = "false") boolean slow) {

    return asyncService.generateSummary(id, fail, slow)
        .thenApply(result -> ResponseEntity.ok(Map.of("summary", result)));
  }

  @PostMapping("/overload")
  public ResponseEntity<Map<String, String>> overload() {
    overloadService.triggerOverload();

    return ResponseEntity.accepted().body(
        Map.of("message", "Tasks scheduled"));
  }
}