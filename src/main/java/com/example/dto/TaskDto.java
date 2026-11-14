package com.example.dto;

import lombok.AllArgsConstructor;
import com.example.annotation.ValidTaskDetails;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidTaskDetails
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
}