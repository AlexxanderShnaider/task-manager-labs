
package com.example.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TaskAsyncWorker {

  private static final Logger log = LoggerFactory.getLogger(TaskAsyncWorker.class);

  @Async("taskAsyncExecutor")
  public void sendNotification(Long taskId, boolean fail) {
    simulateDelay(1000);

    if (fail) {
      throw new IllegalStateException("Notification delivery failed");
    }

    log.info("Notification sent for task {}, thread={}",
        taskId, Thread.currentThread().getName());
  }

  @Async("taskAsyncExecutor")
  public CompletableFuture<String> generateReport(Long taskId) {
    simulateDelay(1000);

    String report = "Report generated for task " + taskId;

    log.info("Report generated, thread={}",
        Thread.currentThread().getName());

    return CompletableFuture.completedFuture(report);
  }

  @Async("taskAsyncExecutor")
  public CompletableFuture<String> checkExternalStatus(
      Long taskId, boolean fail, boolean slow) {

    simulateDelay(slow ? 4000 : 1000);

    if (fail) {
      throw new IllegalStateException("External service unavailable");
    }

    String status = "External status OK for task " + taskId;

    log.info("External status checked, thread={}",
        Thread.currentThread().getName());

    return CompletableFuture.completedFuture(status);
  }

  private void simulateDelay(long milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Async operation interrupted", e);
    }
  }
}
