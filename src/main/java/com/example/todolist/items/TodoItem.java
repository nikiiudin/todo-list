package com.example.todolist.items;

import com.example.todolist.constants.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class TodoItem {

    @Id
    @GeneratedValue
    private Long id;
    private String description;
    private Status status;
    private LocalDateTime creationDateTime;
    private LocalDateTime dueDateTime;
    private LocalDateTime completionDateTime;

}
