package application.registry;

import application.handler.TaskHandler;
import domain.model.TaskType;

import java.util.Map;
import java.util.Optional;

public class TaskHandlerRegistry {

    private final Map<TaskType, TaskHandler> handlers;

    public TaskHandlerRegistry(Map<TaskType, TaskHandler> handlers) {
        this.handlers = handlers;
    }

    public TaskHandler getHandler(TaskType type) {
        return Optional.ofNullable(handlers.get(type))
                .orElseThrow(() -> new IllegalArgumentException("No handler for " + type));
    }
}