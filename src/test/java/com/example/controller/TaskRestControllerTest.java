
package com.example.controller;

import java.util.List;

import com.example.dto.TaskDto;
import com.example.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRestControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskRestController controller;

    @Test
    void getAllTasks_returnsOkAndTaskList() {
        TaskDto dto = new TaskDto(
                1L, "Task", "Description", false
        );

        when(taskService.getAllTasks()).thenReturn(List.of(dto));

        ResponseEntity<List<TaskDto>> response = controller.getAllTasks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(dto), response.getBody());

        verify(taskService).getAllTasks();
    }

    @Test
    void getTaskById_existingId_returnsOk() {
        Long id = 1L;
        TaskDto dto = new TaskDto(
                id, "Task", "Description", false
        );

        when(taskService.getTaskById(id)).thenReturn(dto);

        ResponseEntity<TaskDto> response = controller.getTaskById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());

        verify(taskService).getTaskById(id);
    }

    @Test
    void createTask_validDto_returnsCreated() {
        TaskDto input = new TaskDto(
                null, "New task", "Description", false
        );

        TaskDto created = new TaskDto(
                10L, "New task", "Description", false
        );

        when(taskService.createTask(input)).thenReturn(created);

        ResponseEntity<TaskDto> response = controller.createTask(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(created, response.getBody());

        verify(taskService).createTask(input);
    }

    @Test
    void updateTask_existingId_returnsUpdatedTask() {
        Long id = 1L;
        TaskDto dto = new TaskDto(
                id, "Updated", "Description", true
        );

        when(taskService.updateTask(id, dto)).thenReturn(dto);

        ResponseEntity<TaskDto> response = controller.updateTask(id, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());

        verify(taskService).updateTask(id, dto);
    }

    @Test
    void deleteTask_existingId_returnsNoContent() {
        Long id = 1L;

        ResponseEntity<Void> response = controller.deleteTask(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(taskService).deleteTask(id);
    }
}
