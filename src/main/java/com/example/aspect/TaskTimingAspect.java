package com.example.aspect;

import com.example.service.TaskTimingMetrics;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class TaskTimingAspect {

    private final TaskTimingMetrics metrics;

    @Around("@annotation(com.example.annotation.MeasureTaskDuration)")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {

        long started = System.nanoTime();

        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.nanoTime() - started;

            String method = joinPoint.getSignature().getDeclaringTypeName()
                    + "." + joinPoint.getSignature().getName();

            metrics.record(method, duration);
        }
    }
}