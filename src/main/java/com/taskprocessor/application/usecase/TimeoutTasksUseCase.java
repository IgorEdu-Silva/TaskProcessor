package com.taskprocessor.application.usecase;

import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.model.TaskStatus;
import com.taskprocessor.domain.service.TaskLifecycle;

import java.time.Duration;

public record TimeoutTasksUseCase(
        TaskRepositoryPort repository,
        TaskLifecycle lifecycle,
        Duration timeout
) {

    public void execute() {
        repository.findProcessingTasks().stream()
                .filter(task -> lifecycle.isTimedOut(task, timeout))
                .forEach(this::fail);
    }

    private void fail(Task task) {
        repository.saveWhenStatus(lifecycle.timeout(task), TaskStatus.PROCESSING);
    }
}
