package application.registry;

import application.handler.TaskHandler;
import domain.model.TaskType;

import java.util.Map;

public class TaskHandlerRegistry {

    private final Map<TaskType, TaskHandler> handlers;

    public TaskHandlerRegistry(Map<TaskType, TaskHandler> handlers) {
        this.handlers = handlers;
    }

    public TaskHandler getHandler(TaskType type) {
        TaskHandler handler = handlers.get(type);

        if (handler == null) {
            throw new IllegalArgumentException("No handler for task type " + type);
        }

        return handler;
    }
}