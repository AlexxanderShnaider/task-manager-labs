package com.example.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TaskIdAspect {

  @Before("@annotation(com.example.annotation.RequirePositiveTaskId)")
  public void validate(JoinPoint joinPoint) {

    Object[] arguments = joinPoint.getArgs();

    if (arguments.length == 0 || !(arguments[0] instanceof Long)) {
      throw new IllegalArgumentException("Task ID must be a positive number");
    }

    Long id = (Long) arguments[0];

    if (id <= 0) {
      throw new IllegalArgumentException("Task ID must be a positive number");
    }
  }
}