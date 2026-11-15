package com.example.service;

import com.example.controller.AsyncTaskExceptionHandler;
import com.example.controller.TaskAsyncController;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskAsyncOverloadTest {

  @Test
  void thirdTaskIsRejectedWhenExecutorIsFull() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    executor.setCorePoolSize(1);
    executor.setMaxPoolSize(1);
    executor.setQueueCapacity(1);
    executor.setThreadNamePrefix("overload-test-");
    executor.initialize();

    try {
      TaskAsyncOverloadService service = new TaskAsyncOverloadService(executor);

      assertThrows(
          TaskRejectedException.class,
          service::triggerOverload);
    } finally {
      executor.shutdown();
    }
  }

  @Test
  void rejectedTaskReturnsHttp503() throws Exception {
    TaskAsyncService asyncService = mock(TaskAsyncService.class);

    TaskAsyncOverloadService overloadService = mock(TaskAsyncOverloadService.class);

    doThrow(new TaskRejectedException("Queue full"))
        .when(overloadService)
        .triggerOverload();

    MockMvc mvc = MockMvcBuilders.standaloneSetup(
        new TaskAsyncController(
            asyncService,
            overloadService))
        .setControllerAdvice(new AsyncTaskExceptionHandler())
        .build();

    mvc.perform(post("/api/tasks/async/overload"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.error").value(
            "Async queue is full. Try again later."));
  }
}