package com.example.todolist.services;

import com.example.todolist.repositories.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PastDueScheduler {

    @Autowired
    private TodoRepository todoRepository;

    @Scheduled(fixedDelay = 60000) // Run every 60 seconds
    public void markTodoItemsAsPastDue() {
        todoRepository.markItemsAsPastDue(LocalDateTime.now());
    }
}
