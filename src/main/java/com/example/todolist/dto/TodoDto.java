package com.example.todolist.dto;

import com.example.todolist.constants.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class TodoDto {
    Long id;
    String description;
    Status status;
    LocalDateTime creationDateTime;
    LocalDateTime dueDateTime;
    LocalDateTime completionDateTime;
}
