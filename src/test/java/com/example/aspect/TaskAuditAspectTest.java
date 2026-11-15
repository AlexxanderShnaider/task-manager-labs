
package com.example.aspect;

import com.example.dto.TaskDto;
import com.example.service.TaskAuditTrail;
import com.example.service.TaskService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskAuditAspectTest {

  private ProceedingJoinPoint createJoinPoint() throws Exception {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);

    when(joinPoint.getSignature()).thenReturn(signature);
    when(signature.getMethod()).thenReturn(
        TaskService.class.getMethod("createTask", TaskDto.class));

    return joinPoint;
  }

  @Test
  void audit_recordsSuccessfulTaskCreation() throws Throwable {
    TaskAuditTrail auditTrail = mock(TaskAuditTrail.class);
    TaskAuditAspect aspect = new TaskAuditAspect(auditTrail);

    ProceedingJoinPoint joinPoint = createJoinPoint();

    TaskDto task = new TaskDto(
        42L, "Write tests", "Add AOP tests", false);

    when(joinPoint.proceed()).thenReturn(task);

    Object result = aspect.audit(joinPoint);

    assertSame(task, result);
    verify(joinPoint).proceed();
    verify(auditTrail).add("CREATED", 42L);
  }

  @Test
  void audit_doesNotRecordFailedOperation() throws Throwable {
    TaskAuditTrail auditTrail = mock(TaskAuditTrail.class);
    TaskAuditAspect aspect = new TaskAuditAspect(auditTrail);

    ProceedingJoinPoint joinPoint = createJoinPoint();

    when(joinPoint.proceed())
        .thenThrow(new IllegalStateException("Save failed"));

    assertThrows(
        IllegalStateException.class,
        () -> aspect.audit(joinPoint));

    verifyNoInteractions(auditTrail);
  }
}
