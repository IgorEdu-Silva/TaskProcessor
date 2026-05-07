package com.taskprocessor.infra.handler;

import com.taskprocessor.application.handler.TaskHandler;
import com.taskprocessor.domain.model.Task;

public class GenerateReportHandler implements TaskHandler {

    @Override
    public boolean execute(Task task) {
        System.out.println("Generating report: " + task.getPayload());
        return true;
    }
}