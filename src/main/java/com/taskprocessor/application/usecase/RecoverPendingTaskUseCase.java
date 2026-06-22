package com.taskprocessor.application.usecase;

import com.taskprocessor.application.port.TaskDispatchResult;
import com.taskprocessor.application.port.TaskProcessor;
import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.domain.model.Task;

import java.util.UUID;

public class RecoverPendingTaskUseCase {
    private static final System.Logger LOGGER = System.getLogger(RecoverPendingTaskUseCase.class.getName());
    private static final int DEFAULT_LIMIT = 1_000;

    private final TaskRepositoryPort repository;
    private final TaskProcessor processor;
    private final int limit;

    public RecoverPendingTaskUseCase(TaskRepositoryPort repository, TaskProcessor processor) {
        this(repository, processor, DEFAULT_LIMIT);
    }

    public RecoverPendingTaskUseCase(TaskRepositoryPort repository, TaskProcessor processor, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be greater than zero");
        }

        this.repository = repository;
        this.processor = processor;
        this.limit = limit;
    }

    public void execute() {
        repository.findPendingTasks(limit).stream()
                .map(Task::id)
                .forEach(this::dispatch);
    }

    private void dispatch(UUID taskId) {
        TaskDispatchResult result = processor.enqueue(taskId);

        if (result != TaskDispatchResult.ACCEPTED) {
            LOGGER.log(
                    System.Logger.Level.WARNING,
                    "Failed to recover task {0}. Dispatch result: {1}",
                    taskId,
                    result
            );
        }
    }
}
