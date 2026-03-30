package application.usecase;

import application.handler.TaskHandler;
import application.registry.TaskHandlerRegistry;
import domain.model.Task;
import domain.repository.TaskRepository;

public class ProcessTaskUseCase {
    private final TaskRepository repository;
    private final TaskHandlerRegistry handlerRegistry;

    public ProcessTaskUseCase(TaskRepository repository, TaskHandlerRegistry handlerRegistry) {
        this.repository = repository;
        this.handlerRegistry = handlerRegistry;
    }

    public void execute(Long taskId) {
        Task task = repository.findById(taskId).orElseThrow();

        task.startProcessing();

        TaskHandler handler = handlerRegistry.getHandler(task.getType());

        handler.execute(task);

        task.markDone();

        repository.save(task);
    }
}
