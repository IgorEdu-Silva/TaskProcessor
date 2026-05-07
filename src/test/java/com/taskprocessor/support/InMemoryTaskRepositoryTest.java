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
    private final Clock clock;

    public InMemoryTaskRepositoryTest(Clock clock) {
        this.clock = clock;
    }

    private List<Task> findByPredicate(Predicate<Task> predicate) {
        return storage.values().stream()
                .filter(predicate)
                .toList();
    }

    @Override
    public Task save(Task task) {
        storage.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(UUID uuid) {
        return Optional.ofNullable(storage.get(uuid));
    }

    @Override
    public List<Task> findPendingTasks() {
        return findByPredicate(Task::canStartProcessing);
    }

    @Override
    public boolean markAsProcessing(UUID taskId) {
        Task task = storage.get(taskId);
        if (task == null) return false;

        synchronized (task) {
            if (!task.canStartProcessing()) return false;
            task.requestProcessing();
            return true;
        }
    }

    @Override
    public List<Task> findTasksInRetry() {
        return findByPredicate(task -> task.getStatus() == TaskStatus.RETRY);
    }

    @Override
    public List<Task> findProcessingTasks() {
        return findByPredicate(task -> task.getStatus() == TaskStatus.PROCESSING);
    }

    public List<Task> findAll() {
        return new ArrayList<>(storage.values());
    }
}