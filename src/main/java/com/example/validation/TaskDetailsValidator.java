
package com.example.validation;

import com.example.annotation.ValidTaskDetails;
import com.example.dto.TaskDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TaskDetailsValidator
        implements ConstraintValidator<ValidTaskDetails, TaskDto> {

    @Override
    public boolean isValid(
            TaskDto task,
            ConstraintValidatorContext context) {

        if (task == null) {
            return true;
        }

        String title = task.getTitle();
        String description = task.getDescription();

        if (title == null || description == null || description.isBlank()) {
            return true;
        }

        return !title.trim().equalsIgnoreCase(description.trim());
    }
}
