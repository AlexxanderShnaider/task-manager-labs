
package com.example.validation;

import java.util.Set;

import com.example.dto.TaskDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskDetailsValidatorTest {

  private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

  private final Validator validator = factory.getValidator();

  @AfterAll
  static void closeFactory() {
    factory.close();
  }

  @Test
  void validTaskDetails_passValidation() {
    TaskDto task = new TaskDto(
        null, "Write tests", "Add unit tests", false);

    TaskDto withoutDescription = new TaskDto(
        null, "Write tests", "", false);

    assertTrue(validator.validate(task).isEmpty());
    assertTrue(validator.validate(withoutDescription).isEmpty());
  }

  @Test
  void repeatedDescription_returnsViolation() {
    TaskDto task = new TaskDto(
        null, "Write tests", "write tests", false);

    Set<ConstraintViolation<TaskDto>> violations = validator.validate(task);

    assertEquals(1, violations.size());
    assertEquals(
        "Task description must not repeat the title",
        violations.iterator().next().getMessage());
  }
}
