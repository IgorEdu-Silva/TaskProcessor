package com.taskprocessor.application.usecase;

import com.taskprocessor.application.command.CreateTaskCommand;
import com.taskprocessor.application.port.TaskProcessor;
import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.service.TaskLifecycle;

import java.util.UUID;

public class CreateTaskUseCase {

    private final TaskRepositoryPort repository;
    private final TaskProcessor processor;
    private final TaskLifecycle lifecycle;

    public CreateTaskUseCase(TaskRepositoryPort repository,
                             TaskProcessor processor,
                             TaskLifecycle lifecycle) {
        this.repository = repository;
        this.processor = processor;
        this.lifecycle = lifecycle;
    }

    public UUID execute(CreateTaskCommand command) {
        Task task = lifecycle.create(command.type(), command.payload());
        repository.save(task);
        processor.enqueue(task.id());
        return task.id();
    }
}
