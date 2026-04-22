package application.usecase;

import application.handler.TaskHandler;
import application.port.TaskRepositoryPort;
import application.registry.TaskHandlerRegistry;
import domain.model.Task;

import java.util.UUID;
import java.util.function.Consumer;

public class ProcessTaskUseCase {

    private final TaskRepositoryPort repository;
    private final TaskHandlerRegistry handlerRegistry;

    public ProcessTaskUseCase(TaskRepositoryPort repository,
                              TaskHandlerRegistry handlerRegistry) {
        this.repository = repository;
        this.handlerRegistry = handlerRegistry;
    }

    public void execute(UUID taskId) {

        if (!repository.markAsProcessing(taskId)) return;

        Task task = repository.findById(taskId).orElseThrow();
        TaskHandler handler = handlerRegistry.getHandler(task.getType());

        runWithState(task, handler::execute, Task::complete, Task::fail);
    }

    private void runWithState(
            Task task,
            Consumer<Task> action,
            Consumer<Task> onSuccess,
            Consumer<Task> onError
    ) {
        try {
            action.accept(task);
            onSuccess.accept(task);
        } catch (Exception e) {
            onError.accept(task);
            throw e;
        } finally {
            repository.save(task);
        }
    }
}