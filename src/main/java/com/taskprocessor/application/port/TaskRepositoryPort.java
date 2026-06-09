package com.taskprocessor.application.port;

import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.model.TaskStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepositoryPort {
    Task save(Task task);
    boolean saveWhenStatus(Task task, TaskStatus expectedStatus);
    Optional<Task> findById(UUID id);
    List<Task> findPendingTasks();
    List<Task> findProcessingTasks();
    List<Task> findTasksInRetry();
}
