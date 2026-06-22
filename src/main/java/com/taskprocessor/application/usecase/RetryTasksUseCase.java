package com.taskprocessor.application.usecase;

import com.taskprocessor.application.port.TaskProcessor;
import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.application.port.TaskDispatchResult;
import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.model.TaskStatus;
import com.taskprocessor.domain.service.TaskLifecycle;

public class RetryTasksUseCase {
    private static final System.Logger LOGGER = System.getLogger(RetryTasksUseCase.class.getName());

    private final TaskRepositoryPort repository;
    private final TaskProcessor processor;
    private final TaskLifecycle lifecycle;

    public RetryTasksUseCase(TaskRepositoryPort repository,
                             TaskProcessor processor,
                             TaskLifecycle lifecycle) {
        this.repository = repository;
        this.processor = processor;
        this.lifecycle = lifecycle;
    }

    public void execute() {
        repository.findTasksInRetry().stream()
                .filter(lifecycle::isRetryDue)
                .forEach(this::retry);
    }

    private void retry(Task task) {
        Task pending = lifecycle.markForRetry(task);
        if (repository.saveWhenStatus(pending, TaskStatus.RETRY)) {
            TaskDispatchResult result = processor.enqueue(pending.id());

            if (result != TaskDispatchResult.ACCEPTED) {
                LOGGER.log(
                        System.Logger.Level.WARNING,
                        "Task {0} was marked pending but not dispatched immediately. Dispatch result: {1}",
                        pending.id(),
                        result
                );
            }
        }
    }
}
