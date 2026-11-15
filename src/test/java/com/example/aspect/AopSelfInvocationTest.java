package com.example.aspect;

import com.example.service.TaskAopDemoService;
import com.example.service.TaskTimingMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(AopSelfInvocationTest.Config.class)
class AopSelfInvocationTest {

  private static final String METHOD = TaskAopDemoService.class.getName() + ".measuredStep";

  @Configuration
  @EnableAspectJAutoProxy(proxyTargetClass = true)
  @Import({
      TaskAopDemoService.class,
      TaskTimingAspect.class,
      TaskTimingMetrics.class
  })
  static class Config {
  }

  @Autowired
  private TaskAopDemoService demoService;

  @Autowired
  private TaskTimingMetrics metrics;

  @BeforeEach
  void resetMetrics() {
    metrics.clear();
  }

  @Test
  void externalCall_triggersAspect() {
    String result = demoService.measuredStep();

    assertEquals("Task operation executed", result);
    assertEquals(1, metrics.getCount(METHOD));
  }

  @Test
  void selfInvocation_doesNotTriggerAspect() {
    String result = demoService.invokeInternally();

    assertEquals("Task operation executed", result);
    assertEquals(0, metrics.getCount(METHOD));
  }
}