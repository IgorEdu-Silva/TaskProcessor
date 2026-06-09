package com.taskprocessor.application.handler;

import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.result.TaskResult;

import java.util.concurrent.atomic.AtomicInteger;

public class FlakyHandlerTest implements TaskHandler {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public TaskResult execute(Task task) {
        if (counter.incrementAndGet() < 3) {
            throw new RuntimeException("fail");
        }
        return TaskResult.success();
    }
}
