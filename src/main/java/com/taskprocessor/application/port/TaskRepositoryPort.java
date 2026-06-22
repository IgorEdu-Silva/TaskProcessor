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
    List<Task> findPendingTasks(int limit);
    default List<Task> findPendingTasks() {
        return findPendingTasks(Integer.MAX_VALUE);
    }
    List<Task> findProcessingTasks();
    List<Task> findTasksInRetry();
}
