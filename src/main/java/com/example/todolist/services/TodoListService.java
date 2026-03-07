package com.example.todolist.services;

import com.example.todolist.constants.Status;
import com.example.todolist.dto.TodoDto;
import com.example.todolist.items.TodoItem;
import com.example.todolist.repositories.TodoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TodoListService {

    @Autowired
    private TodoRepository todoRepository;
    private final ModelMapper mapper = new ModelMapper();


    public void createTodo(TodoDto todoDto) {
        todoRepository.save(mapToEntity(todoDto));
    }

    public void updateTodoDescription(Long id, String description) {
        TodoItem item = todoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Todo item not found with id: " + id));
        item.setDescription(description);
        todoRepository.save(item);
    }

    public void updateTodoStatus(Long id, Status status) {
        TodoItem item = todoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Todo item not found with id: " + id));
        item.setStatus(status);
        todoRepository.save(item);
    }

    public TodoDto getTodoItemById(Long id) {
        return todoRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow();
    }

    public List<TodoDto> getTodoList(boolean onlyNotDone) {
        List<TodoItem> list = onlyNotDone ? todoRepository.findAllByStatus(Status.NOT_DONE) : todoRepository.findAll();
        return list.stream()
                .map(this::mapToDto)
                .toList();
    }

    private TodoItem mapToEntity(TodoDto dto) {
        try {
            return mapper.map(dto, TodoItem.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error mapping TodoDto to TodoItem", e);
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
