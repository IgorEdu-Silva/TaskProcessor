package com.taskprocessor.infra.handler;

import com.taskprocessor.application.handler.TaskHandler;
import com.taskprocessor.domain.model.Task;

public class DataProcessingHandler implements TaskHandler {

    @Override
    public boolean execute(Task task) {
        System.out.println("Processing data: " + task.getPayload());
        return true;
    }
}