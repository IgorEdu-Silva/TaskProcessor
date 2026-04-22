package application.usecase;

import application.port.TaskRepositoryPort;
import domain.model.Task;

public class TimeoutTasksUseCase {

    private final TaskRepositoryPort repository;

    public TimeoutTasksUseCase(TaskRepositoryPort repository) {
        this.repository = repository;
    }

    public void execute() {
        repository.findProcessingTasks().stream()
                .filter(task -> task.isTimedOut(task.getType().timeout()))
                .forEach(this::fail);
    }

    private void fail(Task task) {
        task.fail();
        repository.save(task);
    }
}