
package com.example.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskAsyncService {

  private static final Logger log = LoggerFactory.getLogger(TaskAsyncService.class);

  private final TaskAsyncWorker worker;

  public void notifyTask(Long taskId, boolean fail) {
    worker.sendNotification(taskId, fail);
  }

  public CompletableFuture<String> generateSummary(
      Long taskId, boolean fail, boolean slow) {

    CompletableFuture<String> report = worker.generateReport(taskId)
        .orTimeout(2, TimeUnit.SECONDS)
        .exceptionally(error -> {
          log.warn("Report generation failed", error);
          return "Report unavailable";
        });

    CompletableFuture<String> status = worker.checkExternalStatus(taskId, fail, slow)
        .orTimeout(2, TimeUnit.SECONDS)
        .exceptionally(error -> {
          log.warn("External status check failed", error);
          return "External status unavailable";
        });

    return report.thenCombine(
        status,
        (reportResult, statusResult) -> reportResult + " | " + statusResult);
  }
}
