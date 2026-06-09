package com.taskprocessor.infra.handler;

import com.taskprocessor.application.handler.TaskHandler;
import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.result.TaskResult;

public class GenerateReportHandler implements TaskHandler {

    @Override
    public TaskResult execute(Task task) {
        System.out.println("Generating report: " + task.payload());
        return TaskResult.success();
    }
}
