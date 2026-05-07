package com.taskprocessor.application.usecase;

import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.domain.model.Task;

import java.time.Duration;

public record TimeoutTasksUseCase(TaskRepositoryPort repository, Duration timeout) {

    public void execute() {
        repository.findProcessingTasks().stream()
                .filter(task -> task.isTimedOut(timeout))
                .forEach(this::fail);
    }

    private void fail(Task task) {
        task.fail();
        repository.save(task);
    }
}