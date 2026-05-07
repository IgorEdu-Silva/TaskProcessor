package com.taskprocessor.application.port;

import com.taskprocessor.domain.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepositoryPort {
    Task save(Task task);
    Optional<Task> findById(UUID id);
    List<Task> findPendingTasks();
    List<Task> findProcessingTasks();
    List<Task> findTasksInRetry();
    boolean markAsProcessing(UUID taskId);
}
