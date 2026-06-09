package com.taskprocessor.application.handler;

import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.result.TaskResult;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TestHandlerTest implements TaskHandler {

    private final Set<UUID> executed = ConcurrentHashMap.newKeySet();

    @Override
    public TaskResult execute(Task task) {
        if (!executed.add(task.id())) {
            throw new IllegalStateException("Duplicate execution: " + task.id());
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {}

        return TaskResult.success();
    }

    public int executionCount() {
        return executed.size();
    }
}
