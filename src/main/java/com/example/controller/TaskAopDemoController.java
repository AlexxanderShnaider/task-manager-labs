package com.example.controller;

import java.util.List;
import java.util.Map;

import com.example.service.TaskAopDemoService;
import com.example.service.TaskAuditTrail;
import com.example.service.TaskTimingMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks/aop")
@RequiredArgsConstructor
public class TaskAopDemoController {

  private static final String MEASURED_METHOD = TaskAopDemoService.class.getName() + ".measuredStep";

  private final TaskAopDemoService demoService;
  private final TaskTimingMetrics metrics;
  private final TaskAuditTrail auditTrail;

  @GetMapping("/external")
  public Map<String, Object> externalCall() {
    return execute(false);
  }

  @GetMapping("/internal")
  public Map<String, Object> internalCall() {
    return execute(true);
  }

  @GetMapping("/audit")
  @PreAuthorize("hasRole('ADMIN')")
  public List<TaskAuditTrail.Entry> auditHistory() {
    return auditTrail.getEntries();
  }

  private Map<String, Object> execute(boolean internal) {
    int before = metrics.getCount(MEASURED_METHOD);

    String result = internal
        ? demoService.invokeInternally()
        : demoService.measuredStep();

    int after = metrics.getCount(MEASURED_METHOD);

    return Map.of(
        "call", internal ? "internal" : "external",
        "result", result,
        "aspectTriggered", after > before,
        "countBefore", before,
        "countAfter", after);
  }
}