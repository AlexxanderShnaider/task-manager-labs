
package com.example.mapper;

import com.example.dto.TaskDto;
import com.example.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskDto toDto(Task task) {
        if (task == null) {
            return null;
        }

        return new TaskDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted()
        );
    }

    public Task toEntity(TaskDto dto) {
        if (dto == null) {
            return null;
        }

        return new Task(
                dto.getId(),
                dto.getTitle(),
                dto.getDescription(),
                dto.isCompleted()
        );
    }
}
