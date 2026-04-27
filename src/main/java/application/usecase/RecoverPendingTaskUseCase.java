package application.usecase;

import application.port.TaskProcessor;
import application.port.TaskRepositoryPort;
import domain.model.Task;

public class RecoverPendingTaskUseCase {
    private final TaskRepositoryPort repository;
    private final TaskProcessor processor;

    public RecoverPendingTaskUseCase(TaskRepositoryPort repository, TaskProcessor processor) {
        this.repository = repository;
        this.processor = processor;
    }

    public void execute() {
        repository.findPendingTasks().stream()
                .map(Task::getId)
                .forEach(processor::enqueue);
    }
}
