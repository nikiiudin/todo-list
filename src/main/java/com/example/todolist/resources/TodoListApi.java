package com.example.todolist.resources;

import com.example.todolist.constants.Status;
import com.example.todolist.dto.TodoDto;
import com.example.todolist.services.TodoListService;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todo")
public class TodoListApi {

    @Autowired
    private TodoListService todoListService;

    @PostMapping
    public void createTodo(@RequestBody TodoDto todoDto) {
        todoListService.createTodo(todoDto);
    }

    @PutMapping("/description")
    public void updateTodoDescription(@RequestParam @Nonnull Long id, @RequestParam @Nonnull String description) {
        todoListService.updateTodoDescription(id, description);
    }

    @PutMapping("/status")
    public void updateTodoStatus(@RequestParam @Nonnull Long id, @RequestParam @Nonnull Status status) {
        if (status == Status.PAST_DUE) {
            throw new IllegalArgumentException("Status cannot be set to PAST_DUE");
        }
        todoListService.updateTodoStatus(id, status);
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
