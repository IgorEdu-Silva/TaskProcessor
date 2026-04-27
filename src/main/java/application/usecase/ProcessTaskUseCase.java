package application.usecase;

import application.port.TaskRepositoryPort;
import application.registry.TaskHandlerRegistry;
import domain.model.Task;

import java.util.Optional;
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

        Optional.of(taskId)
                .filter(repository::markAsProcessing)
                .flatMap(repository::findById)
                .filter(task -> !task.isFinalState())
                .ifPresent(this::process);
    }

    private void process(Task task) {
        var handler = handlerRegistry.getHandler(task.getType());

        runWithState(
                task,
                t -> Optional.of(handler.execute(t))
                        .filter(Boolean::booleanValue)
                        .orElseThrow(),
                Task::complete,
                Task::fail
        );
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