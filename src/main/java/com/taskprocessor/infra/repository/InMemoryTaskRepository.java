package com.taskprocessor.infra.repository;

import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.model.TaskStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTaskRepository implements TaskRepositoryPort {

    private final Map<UUID, Task> storage = new ConcurrentHashMap<>();

    @Override
    public Task save(Task task) {
        storage.put(task.id(), task);
        return task;
    }

    @Override
    public boolean saveWhenStatus(Task task, TaskStatus expectedStatus) {
        synchronized (storage) {
            Task current = storage.get(task.id());
            if (current == null || current.status() != expectedStatus) {
                return false;
            }

            storage.put(task.id(), task);
            return true;
        }
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Task> findPendingTasks() {
        return storage.values().stream()
                .filter(task -> task.status() == TaskStatus.PENDING)
                .toList();
    }

    @Override
    public List<Task> findProcessingTasks() {
        return storage.values().stream()
                .filter(task -> task.status() == TaskStatus.PROCESSING)
                .toList();
    }

    @Override
    public List<Task> findTasksInRetry() {
        return storage.values().stream()
                .filter(task -> task.status() == TaskStatus.RETRY)
                .toList();
    }
}
