package com.taskprocessor.application.exception;

import java.util.UUID;

public class TaskNotFoundException extends ApplicationException {
    public TaskNotFoundException(UUID id) {
        super("TASK_NOT_FOUND", "Task" + id + " not found");
    }
}
