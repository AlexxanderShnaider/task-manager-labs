
package com.example.service;

import java.util.List;
import java.util.Optional;

import com.example.dto.TaskDto;
import com.example.entity.Task;
import com.example.mapper.TaskMapper;
import com.example.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getAllTasks_existingTasks_returnsDtoList() {
        Task task1 = new Task(1L, "Task 1", "Description 1", false);
        Task task2 = new Task(2L, "Task 2", "Description 2", true);

        TaskDto dto1 = new TaskDto(1L, "Task 1", "Description 1", false);
        TaskDto dto2 = new TaskDto(2L, "Task 2", "Description 2", true);

        when(taskRepository.findAll()).thenReturn(List.of(task1, task2));
        when(taskMapper.toDto(task1)).thenReturn(dto1);
        when(taskMapper.toDto(task2)).thenReturn(dto2);

        List<TaskDto> result = taskService.getAllTasks();

        assertEquals(List.of(dto1, dto2), result);

        verify(taskRepository).findAll();
        verify(taskMapper).toDto(task1);
        verify(taskMapper).toDto(task2);
    }

    @Test
    void getAllTasks_emptyRepository_returnsEmptyList() {
        when(taskRepository.findAll()).thenReturn(List.of());

        List<TaskDto> result = taskService.getAllTasks();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findAll();
        verifyNoInteractions(taskMapper);
    }

    @Test
    void getTaskById_existingTask_returnsDto() {
        Long id = 1L;

        Task task = new Task(id, "Test task", "Description", false);
        TaskDto dto = new TaskDto(id, "Test task", "Description", false);

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(dto);

        TaskDto result = taskService.getTaskById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test task", result.getTitle());

        verify(taskRepository).findById(id);
        verify(taskMapper).toDto(task);
    }

    @Test
    void getTaskById_nonExistingTask_throwsException() {
        Long id = 999L;

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.getTaskById(id)
        );

        assertEquals("Task with ID 999 not found", exception.getMessage());

        verify(taskRepository).findById(id);
        verifyNoInteractions(taskMapper);
    }

    @Test
    void createTask_validDto_savesTaskAndReturnsDto() {
        TaskDto inputDto = new TaskDto(
                null, "New task", "Description", false
        );

        Task task = new Task(
                null, "New task", "Description", false
        );

        Task savedTask = new Task(
                10L, "New task", "Description", false
        );

        TaskDto expectedDto = new TaskDto(
                10L, "New task", "Description", false
        );

        when(taskMapper.toEntity(inputDto)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(savedTask);
        when(taskMapper.toDto(savedTask)).thenReturn(expectedDto);

        TaskDto result = taskService.createTask(inputDto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("New task", result.getTitle());
        assertFalse(result.isCompleted());

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        verify(taskMapper).toEntity(inputDto);
        verify(taskRepository).save(captor.capture());
        verify(taskMapper).toDto(savedTask);

        Task capturedTask = captor.getValue();

        assertSame(task, capturedTask);
        assertEquals("New task", capturedTask.getTitle());
        assertEquals("Description", capturedTask.getDescription());
        assertFalse(capturedTask.isCompleted());
    }

    @Test
    void updateTask_existingTask_updatesFieldsAndSaves() {
        Long id = 1L;

        Task existingTask = new Task(
                id, "Old title", "Old description", false
        );

        TaskDto inputDto = new TaskDto(
                id, "Updated title", "Updated description", true
        );

        TaskDto expectedDto = new TaskDto(
                id, "Updated title", "Updated description", true
        );

        when(taskRepository.findById(id)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toDto(existingTask)).thenReturn(expectedDto);

        TaskDto result = taskService.updateTask(id, inputDto);

        assertNotNull(result);
        assertEquals("Updated title", result.getTitle());
        assertEquals("Updated description", result.getDescription());
        assertTrue(result.isCompleted());

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        verify(taskRepository).findById(id);
        verify(taskRepository).save(captor.capture());
        verify(taskMapper).toDto(existingTask);

        Task updatedTask = captor.getValue();

        assertSame(existingTask, updatedTask);
        assertEquals("Updated title", updatedTask.getTitle());
        assertEquals("Updated description", updatedTask.getDescription());
        assertTrue(updatedTask.isCompleted());
    }

    @Test
    void updateTask_nonExistingTask_throwsException() {
        Long id = 999L;

        TaskDto inputDto = new TaskDto(
                id, "Updated", "Description", true
        );

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.updateTask(id, inputDto)
        );

        assertEquals("Task with ID 999 not found", exception.getMessage());

        verify(taskRepository).findById(id);
        verify(taskRepository, never()).save(any(Task.class));
        verifyNoInteractions(taskMapper);
    }

    @Test
    void deleteTask_existingId_callsRepositoryDelete() {
        Long id = 1L;

        taskService.deleteTask(id);

        verify(taskRepository).deleteById(id);
        verifyNoInteractions(taskMapper);
    }
}
