package com.taskprocessor.application.usecase;

import com.taskprocessor.application.command.CreateTaskCommand;
import com.taskprocessor.application.factory.TaskFactory;
import com.taskprocessor.application.port.TaskProcessor;
import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.domain.model.Task;

import java.util.UUID;

public class CreateTaskUseCase {

    private final TaskRepositoryPort repository;
    private final TaskProcessor processor;
    private final TaskFactory factory;

    public CreateTaskUseCase(TaskRepositoryPort repository,
                             TaskProcessor processor,
                             TaskFactory factory) {
        this.repository = repository;
        this.processor = processor;
        this.factory = factory;
    }

    public UUID execute(CreateTaskCommand command) {
        Task task = factory.create(command.type(), command.payload());
        repository.save(task);
        processor.enqueue(task.getId());
        return task.getId();
    }
}