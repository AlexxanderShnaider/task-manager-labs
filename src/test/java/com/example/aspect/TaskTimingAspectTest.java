package com.example.aspect;

import com.example.service.TaskTimingMetrics;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskTimingAspectTest {

  private final TaskTimingMetrics metrics = new TaskTimingMetrics();
  private final TaskTimingAspect aspect = new TaskTimingAspect(metrics);

  private ProceedingJoinPoint createJoinPoint() {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    Signature signature = mock(Signature.class);

    when(joinPoint.getSignature()).thenReturn(signature);
    when(signature.getDeclaringTypeName()).thenReturn("Demo");
    when(signature.getName()).thenReturn("run");

    return joinPoint;
  }

  @Test
  void measure_callsProceedAndRecordsExecution() throws Throwable {
    ProceedingJoinPoint joinPoint = createJoinPoint();

    when(joinPoint.proceed()).thenReturn("success");

    Object result = aspect.measure(joinPoint);

    assertEquals("success", result);
    assertEquals(1, metrics.getCount("Demo.run"));
    assertTrue(metrics.getLastDurationNanos("Demo.run") >= 0);

    verify(joinPoint).proceed();
  }

  @Test
  void measure_recordsExecutionEvenWhenMethodThrows() throws Throwable {
    ProceedingJoinPoint joinPoint = createJoinPoint();

    when(joinPoint.proceed())
        .thenThrow(new IllegalStateException("Execution failed"));

    assertThrows(
        IllegalStateException.class,
        () -> aspect.measure(joinPoint));

    assertEquals(1, metrics.getCount("Demo.run"));
    verify(joinPoint).proceed();
  }
}