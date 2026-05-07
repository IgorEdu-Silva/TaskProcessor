package com.taskprocessor.entry.controller;

import com.taskprocessor.application.command.CreateTaskCommand;
import com.taskprocessor.application.usecase.CreateTaskUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final CreateTaskUseCase useCase;

    public TaskController(CreateTaskUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<UUID> create(@RequestBody CreateTaskCommand command) {
        UUID id = useCase.execute(command);
        return ResponseEntity.ok(id);
    }
}