package com.example.todolist.services;

import com.example.todolist.constants.Status;
import com.example.todolist.dto.CreateTodoDto;
import com.example.todolist.dto.TodoDto;
import com.example.todolist.exceptions.TodoNotFoundException;
import com.example.todolist.items.TodoItem;
import com.example.todolist.repositories.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoListServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoListService todoListService;

    private TodoItem sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = new TodoItem();
        sampleItem.setId(1L);
        sampleItem.setDescription("Buy groceries");
        sampleItem.setStatus(Status.NOT_DONE);
        sampleItem.setCreationDateTime(LocalDateTime.of(2026, 3, 1, 10, 0));
        sampleItem.setDueDateTime(LocalDateTime.of(2026, 3, 10, 10, 0));
    }


    @Test
    void createTodo_shouldSaveEntity() {
        CreateTodoDto dto = new CreateTodoDto("Buy groceries", LocalDateTime.of(2026, 3, 10, 10, 0));
        when(todoRepository.save(any(TodoItem.class))).thenReturn(sampleItem);

        todoListService.createTodo(dto);

        ArgumentCaptor<TodoItem> captor = ArgumentCaptor.forClass(TodoItem.class);
        verify(todoRepository).save(captor.capture());
        TodoItem saved = captor.getValue();
        assertThat(saved.getDescription()).isEqualTo("Buy groceries");
        assertThat(saved.getStatus()).isEqualTo(Status.NOT_DONE);
    }


    @Test
    void updateTodoDescription_shouldUpdateAndSave() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(todoRepository.save(any(TodoItem.class))).thenReturn(sampleItem);

        todoListService.updateTodoDescription(1L, "Updated description");

        ArgumentCaptor<TodoItem> captor = ArgumentCaptor.forClass(TodoItem.class);
        verify(todoRepository).save(captor.capture());
        assertThat(captor.getValue().getDescription()).isEqualTo("Updated description");
    }

    @Test
    void updateTodoDescription_whenNotFound_shouldThrow() {
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoListService.updateTodoDescription(99L, "desc"))
                .isInstanceOf(TodoNotFoundException.class)
                .hasMessageContaining("Todo item not found with id: 99");
    }


    @Test
    void updateTodoStatus_shouldUpdateAndSave() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(todoRepository.save(any(TodoItem.class))).thenReturn(sampleItem);

        todoListService.updateTodoStatus(1L, Status.DONE);

        ArgumentCaptor<TodoItem> captor = ArgumentCaptor.forClass(TodoItem.class);
        verify(todoRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Status.DONE);
    }

    @Test
    void updateTodoStatus_whenNotFound_shouldThrow() {
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoListService.updateTodoStatus(99L, Status.DONE))
                .isInstanceOf(TodoNotFoundException.class)
                .hasMessageContaining("Todo item not found with id: 99");
    }


    @Test
    void getTodoItemById_shouldReturnDto() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleItem));

        TodoDto result = todoListService.getTodoItemById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Buy groceries");
        assertThat(result.getStatus()).isEqualTo(Status.NOT_DONE);
    }

    @Test
    void getTodoItemById_whenNotFound_shouldThrow() {
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoListService.getTodoItemById(99L))
                .isInstanceOf(TodoNotFoundException.class);
    }


    @Test
    void getTodoList_whenOnlyNotDoneTrue_shouldReturnOnlyNotDoneItems() {
        when(todoRepository.findAllByStatus(Status.NOT_DONE)).thenReturn(List.of(sampleItem));

        List<TodoDto> result = todoListService.getTodoList(true);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Buy groceries");
        verify(todoRepository).findAllByStatus(Status.NOT_DONE);
        verify(todoRepository, never()).findAll();
    }

    @Test
    void getTodoList_whenOnlyNotDoneFalse_shouldReturnAllItems() {
        TodoItem doneItem = new TodoItem();
        doneItem.setId(2L);
        doneItem.setDescription("Clean house");
        doneItem.setStatus(Status.DONE);

        when(todoRepository.findAll()).thenReturn(List.of(sampleItem, doneItem));

        List<TodoDto> result = todoListService.getTodoList(false);

        assertThat(result).hasSize(2);
        verify(todoRepository).findAll();
        verify(todoRepository, never()).findAllByStatus(any());
    }

    @Test
    void getTodoList_whenEmpty_shouldReturnEmptyList() {
        when(todoRepository.findAll()).thenReturn(Collections.emptyList());

        List<TodoDto> result = todoListService.getTodoList(false);

        assertThat(result).isEmpty();
    }
}

