
package com.example.mapper;

import com.example.dto.TaskDto;
import com.example.entity.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskMapperTest {

    private final TaskMapper mapper = new TaskMapper();

    @Test
    void toDto_validEntity_mapsAllFields() {
        Task task = new Task(
                1L, "Task title", "Description", true
        );

        TaskDto result = mapper.toDto(task);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Task title", result.getTitle());
        assertEquals("Description", result.getDescription());
        assertTrue(result.isCompleted());
    }

    @Test
    void toEntity_validDto_mapsAllFields() {
        TaskDto dto = new TaskDto(
                2L, "Another task", "Details", false
        );

        Task result = mapper.toEntity(dto);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Another task", result.getTitle());
        assertEquals("Details", result.getDescription());
        assertFalse(result.isCompleted());
    }

    @Test
    void mapper_nullInput_returnsNull() {
        assertNull(mapper.toDto(null));
        assertNull(mapper.toEntity(null));
    }
}
