package com.example.todolist.services;

import com.example.todolist.constants.Status;
import com.example.todolist.dto.CreateTodoDto;
import com.example.todolist.dto.TodoDto;
import com.example.todolist.exceptions.TodoNotFoundException;
import com.example.todolist.items.TodoItem;
import com.example.todolist.repositories.TodoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TodoListService {

    @Autowired
    private TodoRepository todoRepository;
    private final ModelMapper mapper = new ModelMapper();


    public void createTodo(CreateTodoDto newTodoDto) {
        TodoItem newItem = mapToEntity(newTodoDto);
        newItem.setCreationDateTime(LocalDateTime.now());
        newItem.setStatus(Status.NOT_DONE);
        todoRepository.save(newItem);
    }

    public void updateTodoDescription(Long id, String description) {
        TodoItem item = foundOrThrow(id);
        item.setDescription(description);
        todoRepository.save(item);
    }

    public void updateTodoStatus(Long id, Status status) {
        TodoItem item = foundOrThrow(id);
        item.setStatus(status);
        if (status == Status.DONE) {
            item.setCompletionDateTime(LocalDateTime.now());
        } else {
            item.setCompletionDateTime(null);
        }
        todoRepository.save(item);
    }

    private TodoItem foundOrThrow(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }

    public TodoDto getTodoItemById(Long id) {
        TodoItem item = foundOrThrow(id);
        return mapToDto(checkIfOverdue(item));
    }

    public List<TodoDto> getTodoList(boolean onlyNotDone) {
        List<TodoItem> list = onlyNotDone ? todoRepository.findAllByStatus(Status.NOT_DONE) : todoRepository.findAll();
        return list.stream()
                .map(this::checkIfOverdue)
                .map(this::mapToDto)
                .toList();
    }

    private TodoItem checkIfOverdue(TodoItem item) {
        if (item.getStatus() == Status.NOT_DONE && item.getDueDateTime() != null && item.getDueDateTime().isBefore(LocalDateTime.now())) {
            item.setStatus(Status.PAST_DUE);
            todoRepository.save(item);
        }
        return item;
    }

    private TodoItem mapToEntity(CreateTodoDto dto) {
        try {
            return mapper.map(dto, TodoItem.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error mapping CreateTodoDto to TodoItem", e);
        }
    }

    private TodoDto mapToDto(TodoItem entity) {
        try {
            return mapper.map(entity, TodoDto.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error mapping TodoItem to TodoDto", e);
        }
    }
}
