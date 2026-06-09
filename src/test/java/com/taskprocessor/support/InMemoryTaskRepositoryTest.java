package com.taskprocessor.support;

import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.model.TaskStatus;

import java.time.Clock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class InMemoryTaskRepositoryTest implements TaskRepositoryPort {

    private final Map<UUID, Task> storage = new ConcurrentHashMap<>();

    public InMemoryTaskRepositoryTest(Clock clock) {
    }

    private List<Task> findByPredicate(Predicate<Task> predicate) {
        return storage.values().stream()
                .filter(predicate)
                .toList();
    }

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
    public Optional<Task> findById(UUID uuid) {
        return Optional.ofNullable(storage.get(uuid));
    }

    @Override
    public List<Task> findPendingTasks() {
        return findByPredicate(task -> task.status() == TaskStatus.PENDING);
    }

    @Override
    public List<Task> findTasksInRetry() {
        return findByPredicate(task -> task.status() == TaskStatus.RETRY);
    }

    @Override
    public List<Task> findProcessingTasks() {
        return findByPredicate(task -> task.status() == TaskStatus.PROCESSING);
    }

    public List<Task> findAll() {
        return new ArrayList<>(storage.values());
    }
}
