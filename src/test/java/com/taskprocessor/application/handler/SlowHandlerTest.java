package com.taskprocessor.application.handler;

import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.result.TaskResult;

public class SlowHandlerTest implements TaskHandler {

    @Override
    public TaskResult execute(Task task) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}

        return TaskResult.success();
    }
}
