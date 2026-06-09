package com.taskprocessor.application.usecase;

import com.taskprocessor.application.exception.TaskHandlerNotFoundException;
import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.application.registry.TaskHandlerRegistry;
import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.model.TaskStatus;
import com.taskprocessor.domain.result.TaskResult;
import com.taskprocessor.domain.service.TaskLifecycle;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ProcessTaskUseCase {

    private final TaskRepositoryPort repository;
    private final TaskHandlerRegistry handlerRegistry;
    private final TaskLifecycle lifecycle;

    public ProcessTaskUseCase(TaskRepositoryPort repository,
                              TaskHandlerRegistry handlerRegistry,
                              TaskLifecycle lifecycle) {
        this.repository = repository;
        this.handlerRegistry = handlerRegistry;
        this.lifecycle = lifecycle;
    }

    public void execute(UUID taskId) {
        repository.findById(taskId)
                .filter(task -> !lifecycle.isFinalState(task))
                .flatMap(this::claimForProcessing)
                .ifPresent(this::process);
    }

    private Optional<Task> claimForProcessing(Task task) {
        if (!lifecycle.canStartProcessing(task)) {
            return Optional.empty();
        }

        Task started = lifecycle.start(task);
        boolean claimed = repository.saveWhenStatus(started, task.status());

        return claimed ? Optional.of(started) : Optional.empty();
    }

    private void process(Task task) {
        try {
            var handler = handlerRegistry.getHandler(task.type());
            TaskResult result = Objects.requireNonNull(
                    handler.execute(task),
                    "handler result must not be null"
            );

            repository.saveWhenStatus(
                    lifecycle.resolve(task, result),
                    TaskStatus.PROCESSING
            );
        } catch (TaskHandlerNotFoundException e) {
            repository.saveWhenStatus(
                    lifecycle.fail(task, false),
                    TaskStatus.PROCESSING
            );
            throw e;
        } catch (Exception e) {
            repository.saveWhenStatus(
                    lifecycle.fail(task),
                    TaskStatus.PROCESSING
            );
            throw e;
        }
    }
}
