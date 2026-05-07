package com.taskprocessor.application.registry;

import com.taskprocessor.application.exception.TaskHandlerNotFoundException;
import com.taskprocessor.application.handler.TaskHandler;
import com.taskprocessor.domain.model.TaskType;

import java.util.Map;
import java.util.Optional;

public class TaskHandlerRegistry {

    private final Map<TaskType, TaskHandler> handlers;

    public TaskHandlerRegistry(Map<TaskType, TaskHandler> handlers) {
        this.handlers = handlers;
    }

    public TaskHandler getHandler(TaskType type) {
        return Optional.ofNullable(handlers.get(type))
                .orElseThrow(() -> new TaskHandlerNotFoundException("No handler for " + type));
    }
}