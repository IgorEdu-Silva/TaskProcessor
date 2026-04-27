package infra.repository;

import application.port.TaskRepositoryPort;
import domain.model.Task;
import domain.model.TaskStatus;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTaskRepository implements TaskRepositoryPort {

    private final Map<UUID, Task> storage = new ConcurrentHashMap<>();

    @Override
    public Task save(Task task) {
        storage.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Task> findPendingTasks() {
        return storage.values().stream()
                .filter(Task::canStartProcessing)
                .toList();
    }

    @Override
    public List<Task> findProcessingTasks() {
        return storage.values().stream()
                .filter(task -> task.getStatus() == TaskStatus.PROCESSING)
                .toList();
    }

    @Override
    public List<Task> findTasksInRetry() {
        return storage.values().stream()
                .filter(task -> task.getStatus() == TaskStatus.RETRY)
                .filter(task -> task.getNextRetryAt() != null)
                .filter(task -> task.getNextRetryAt().isBefore(Instant.now()))
                .toList();
    }

    @Override
    public boolean markAsProcessing(UUID taskId) {
        return Optional.ofNullable(storage.get(taskId))
                .map(task -> {
                    synchronized (task) {
                        return Optional.of(task)
                                .filter(Task::canStartProcessing)
                                .map(t -> {
                                    t.requestProcessing();
                                    return true;
                                })
                                .orElse(false);
                    }
                })
                .orElse(false);
    }
}