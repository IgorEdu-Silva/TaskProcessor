package com.taskprocessor.application.handler;

import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.result.TaskResult;

public interface TaskHandler {
    TaskResult execute(Task task);
}
