package com.example.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class TaskAsyncOverloadService {

  private final ThreadPoolTaskExecutor executor;

  public TaskAsyncOverloadService(
      @Qualifier("overloadDemoExecutor") ThreadPoolTaskExecutor executor) {
    this.executor = executor;
  }

  public void triggerOverload() {
    for (int i = 0; i < 3; i++) {
      executor.execute(() -> {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
    }
  }
}