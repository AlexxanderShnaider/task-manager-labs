package com.example.aspect;

import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskIdAspectTest {

  private final TaskIdAspect aspect = new TaskIdAspect();

  @Test
  void validate_acceptsPositiveId() {
    JoinPoint joinPoint = mock(JoinPoint.class);

    when(joinPoint.getArgs()).thenReturn(new Object[] { 10L });

    assertDoesNotThrow(() -> aspect.validate(joinPoint));
  }

  @Test
  void validate_rejectsInvalidId() {
    JoinPoint joinPoint = mock(JoinPoint.class);

    when(joinPoint.getArgs()).thenReturn(new Object[] { 0L });

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> aspect.validate(joinPoint));

    assertEquals(
        "Task ID must be a positive number",
        exception.getMessage());
  }
}