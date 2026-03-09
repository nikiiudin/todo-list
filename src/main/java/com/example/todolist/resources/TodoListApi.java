package com.example.todolist.resources;

import com.example.todolist.constants.Status;
import com.example.todolist.dto.CreateTodoDto;
import com.example.todolist.dto.TodoDto;
import com.example.todolist.exceptions.TodoBadRequestException;
import com.example.todolist.exceptions.TodoForbiddenException;
import com.example.todolist.services.TodoListService;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todo")
public class TodoListApi {

    @Autowired
    private TodoListService todoListService;

    @PostMapping
    public ResponseEntity<String> createTodo(@RequestBody CreateTodoDto newTodoDto) {
        todoListService.createTodo(newTodoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Created");
    }

    @PutMapping("/description")
    public ResponseEntity<String> updateTodoDescription(@RequestBody TodoDto todoDto) {
        if (todoDto.getId() == null || todoDto.getDescription() == null || todoDto.getDescription().isBlank()) {
            throw new TodoBadRequestException("Id and description must be provided and not blank");
        }
        todoListService.updateTodoDescription(todoDto.getId(), todoDto.getDescription());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Updated");
    }

    @PutMapping("/status")
    public ResponseEntity<String> updateTodoStatus(@RequestBody TodoDto todoDto) {
        if (todoDto.getId() == null || todoDto.getStatus() == null) {
            throw new TodoBadRequestException("Id and status must be provided");
        }
        if (todoDto.getStatus() == Status.PAST_DUE) {
            throw new TodoForbiddenException("Status cannot be set to PAST_DUE");
        }
        todoListService.updateTodoStatus(todoDto.getId(), todoDto.getStatus());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Updated");
    }

    @GetMapping("/{id}")
    public TodoDto getTodoById(@PathVariable @Nonnull Long id) {
        return todoListService.getTodoItemById(id);
    }

    @GetMapping("/list")
    public List<TodoDto> getTodoList(@RequestParam boolean onlyNotDone) {
        return todoListService.getTodoList(onlyNotDone);
    }

}
