package com.example.todolist.repositories;

import com.example.todolist.constants.Status;
import com.example.todolist.items.TodoItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoRepository extends CrudRepository<TodoItem, Long> {

    List<TodoItem> findAllByStatus(Status status);

    List<TodoItem> findAll();

    @Modifying
    @Transactional
    @Query("UPDATE TodoItem td SET td.status = Status.PAST_DUE " +
            "WHERE td.dueDateTime < :now AND td.status = Status.NOT_DONE")
    void markItemsAsPastDue(@Param("now") LocalDateTime now);

}
