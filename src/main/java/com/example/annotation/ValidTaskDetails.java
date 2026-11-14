
package com.example.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.validation.TaskDetailsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TaskDetailsValidator.class)
public @interface ValidTaskDetails {

    String message() default "Task description must not repeat the title";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
