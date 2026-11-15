
package com.example.aspect;

import com.example.annotation.TaskAudit;
import com.example.dto.TaskDto;
import com.example.service.TaskAuditTrail;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class TaskAuditAspect {

    private final TaskAuditTrail auditTrail;

    @Around("@annotation(com.example.annotation.TaskAudit)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        TaskAudit annotation = signature.getMethod().getAnnotation(TaskAudit.class);

        Object result = joinPoint.proceed();

        Long taskId = null;

        if (result instanceof TaskDto task) {
            taskId = task.getId();
        }

        if (taskId == null) {
            for (Object argument : joinPoint.getArgs()) {
                if (argument instanceof Long id) {
                    taskId = id;
                    break;
                }
            }
        }

        auditTrail.add(annotation.value(), taskId);

        return result;
    }
}
