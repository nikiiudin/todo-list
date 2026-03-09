package com.example.todolist.resources;

import com.example.todolist.constants.Status;
import com.example.todolist.dto.CreateTodoDto;
import com.example.todolist.dto.TodoDto;
import com.example.todolist.services.TodoListService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoListApi.class)
class TodoListApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoListService todoListService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());


    @Test
    void createTodo_shouldReturn201() throws Exception {
        TodoDto dto = new TodoDto(null, "Buy groceries", Status.NOT_DONE,
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                null);

        doNothing().when(todoListService).createTodo(any(CreateTodoDto.class));

        mockMvc.perform(post("/api/todo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(todoListService).createTodo(any(CreateTodoDto.class));
    }


    @Test
    void updateTodoDescription_shouldReturn202() throws Exception {
        doNothing().when(todoListService).updateTodoDescription(1L, "New description");
        TodoDto dto = TodoDto.builder().id(1L).description("New description").build();
        mockMvc.perform(put("/api/todo/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted());

        verify(todoListService).updateTodoDescription(1L, "New description");
    }


    @Test
    void updateTodoStatus_withDone_shouldReturn202() throws Exception {
        doNothing().when(todoListService).updateTodoStatus(1L, Status.DONE);

        TodoDto dto = TodoDto.builder().id(1L).status(Status.DONE).build();
        mockMvc.perform(put("/api/todo/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted());

        verify(todoListService).updateTodoStatus(1L, Status.DONE);
    }

    @Test
    void updateTodoStatus_withPastDue_shouldReturn403() throws Exception {
        TodoDto dto = TodoDto.builder().id(1L).status(Status.PAST_DUE).build();

        mockMvc.perform(put("/api/todo/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(todoListService, never()).updateTodoStatus(anyLong(), any());
    }


    @Test
    void getTodoById_shouldReturnTodo() throws Exception {
        TodoDto dto = new TodoDto(1L, "Buy groceries", Status.NOT_DONE,
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                null);
        when(todoListService.getTodoItemById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/todo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Buy groceries")))
                .andExpect(jsonPath("$.status", is("NOT_DONE")));
    }


    @Test
    void getTodoList_withOnlyNotDoneFalse_shouldReturnAll() throws Exception {
        TodoDto dto1 = new TodoDto(1L, "Buy groceries", Status.NOT_DONE,
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                null);
        TodoDto dto2 = new TodoDto(2L, "Clean house", Status.DONE,
                LocalDateTime.of(2026, 3, 1, 9, 0),
                LocalDateTime.of(2026, 3, 5, 9, 0),
                LocalDateTime.of(2026, 3, 4, 15, 0));

        when(todoListService.getTodoList(false)).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/todo/list")
                        .param("onlyNotDone", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description", is("Buy groceries")))
                .andExpect(jsonPath("$[1].description", is("Clean house")));
    }

    @Test
    void getTodoList_withOnlyNotDoneTrue_shouldReturnFiltered() throws Exception {
        TodoDto dto = new TodoDto(1L, "Buy groceries", Status.NOT_DONE,
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                null);

        when(todoListService.getTodoList(true)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/todo/list")
                        .param("onlyNotDone", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("NOT_DONE")));
    }

    @Test
    void getTodoList_whenEmpty_shouldReturnEmptyArray() throws Exception {
        when(todoListService.getTodoList(false)).thenReturn(List.of());

        mockMvc.perform(get("/api/todo/list")
                        .param("onlyNotDone", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}

