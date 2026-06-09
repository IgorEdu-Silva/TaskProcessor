package com.taskprocessor.infra.handler;

import com.taskprocessor.application.handler.TaskHandler;
import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.result.TaskResult;

public class DataProcessingHandler implements TaskHandler {

    @Override
    public TaskResult execute(Task task) {
        System.out.println("Processing data: " + task.payload());
        return TaskResult.success();
    }
}
