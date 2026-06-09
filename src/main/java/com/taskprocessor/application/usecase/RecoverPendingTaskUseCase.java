package com.taskprocessor.application.usecase;

import com.taskprocessor.application.port.TaskProcessor;
import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.domain.model.Task;

public class RecoverPendingTaskUseCase {
    private final TaskRepositoryPort repository;
    private final TaskProcessor processor;

    public RecoverPendingTaskUseCase(TaskRepositoryPort repository, TaskProcessor processor) {
        this.repository = repository;
        this.processor = processor;
    }

    public void execute() {
        repository.findPendingTasks().stream()
                .map(Task::id)
                .forEach(processor::enqueue);
    }
}
