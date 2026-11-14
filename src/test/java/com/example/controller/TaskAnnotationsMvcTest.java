
package com.example.controller;

import com.example.dto.TaskDto;
import com.example.resolver.CurrentUsernameResolver;
import com.example.service.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskAnnotationsMvcTest {

  private TaskService taskService;
  private MockMvc mockMvc;
  private LocalValidatorFactoryBean validator;

  @BeforeEach
  void setUp() {
    taskService = mock(TaskService.class);

    validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();

    mockMvc = MockMvcBuilders
        .standaloneSetup(new TaskRestController(taskService))
        .setCustomArgumentResolvers(new CurrentUsernameResolver())
        .setValidator(validator)
        .build();
  }

  @AfterEach
  void tearDown() {
    validator.close();
  }

  @Test
  void createTaskEndpoint_returnsCreatedStatus() throws Exception {
    TaskDto createdTask = new TaskDto(
        1L, "Write tests", "Add annotation tests", false);

    when(taskService.createTask(any(TaskDto.class)))
        .thenReturn(createdTask);

    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "title": "Write tests",
              "description": "Add annotation tests",
              "completed": false
            }
            """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Write tests"));

    verify(taskService).createTask(any(TaskDto.class));
  }

  @Test
  void createTaskWithInvalidDetails_returnsBadRequest() throws Exception {
    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "title": "Write tests",
              "description": "Write tests",
              "completed": false
            }
            """))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(taskService);
  }

  @Test
  void whoAmI_usesResolvedUsername() throws Exception {
    mockMvc.perform(get("/api/tasks/whoami")
        .principal(() -> "admin_user"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("admin_user"));

    verifyNoInteractions(taskService);
  }
}
