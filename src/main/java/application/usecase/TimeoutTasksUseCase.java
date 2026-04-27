package application.usecase;

import application.port.TaskRepositoryPort;
import domain.model.Task;

import java.time.Duration;

public class TimeoutTasksUseCase {

    private final TaskRepositoryPort repository;
    private final Duration timeout;

    public TimeoutTasksUseCase(TaskRepositoryPort repository, Duration timeout) {
        this.repository = repository;
        this.timeout = timeout;
    }

    public void execute() {
        repository.findProcessingTasks().stream()
                .filter(task -> task.isTimedOut(timeout))
                .peek(Task::fail)
                .forEach(repository::save);
    }

    private void fail(Task task) {
        task.fail();
        repository.save(task);
    }
}