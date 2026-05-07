package com.taskprocessor.application.handler;

import com.taskprocessor.domain.model.Task;

public interface TaskHandler {
    boolean execute(Task task);
}
