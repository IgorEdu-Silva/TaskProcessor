package application.usecase;

import application.port.TaskProcessor;
import application.port.TaskRepositoryPort;
import domain.model.Task;

public class RetryTasksUseCase {

    private final TaskRepositoryPort repository;
    private final TaskProcessor processor;

    public RetryTasksUseCase(TaskRepositoryPort repository,
                             TaskProcessor processor) {
        this.repository = repository;
        this.processor = processor;
    }

    public void execute() {
        repository.findTasksInRetry().stream()
                .filter(Task::canRetry)
                .forEach(this::retry);
    }

    private void retry(Task task) {
        task.markForRetry();
        repository.save(task);
        processor.enqueue(task.getId());
    }
}