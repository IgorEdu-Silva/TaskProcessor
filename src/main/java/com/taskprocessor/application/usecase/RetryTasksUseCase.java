package com.taskprocessor.application.usecase;

import com.taskprocessor.application.port.TaskProcessor;
import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.domain.model.Task;

import java.time.Instant;

public class RetryTasksUseCase {

    private final TaskRepositoryPort repository;
    private final TaskProcessor processor;

    public RetryTasksUseCase(TaskRepositoryPort repository,
                             TaskProcessor processor) {
        this.repository = repository;
        this.processor = processor;
    }

    public void execute() {
        repository.findTasksInRetry().stream()
                .filter(Task::canRetry)
                .filter(task -> task.getNextRetryAt().isBefore(Instant.now()))
                .peek(Task::markForRetry)
                .peek(repository::save)
                .map(Task::getId)
                .forEach(processor::enqueue);
    }

    private void retry(Task task) {
        task.markForRetry();
        repository.save(task);
        processor.enqueue(task.getId());
    }
}