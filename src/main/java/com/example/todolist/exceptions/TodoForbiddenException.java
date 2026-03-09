package com.example.todolist.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TodoForbiddenException extends RuntimeException {

    public TodoForbiddenException(String message) {
        super(message);
    }
}

