package com.taskprocessor.application.handler;

import com.taskprocessor.domain.model.Task;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TestHandlerTest implements TaskHandler {

    private final Set<UUID> executed = ConcurrentHashMap.newKeySet();

    @Override
    public boolean execute(Task task) {
        if (!executed.add(task.getId())) {
            throw new IllegalStateException("Duplicate execution: " + task.getId());
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {}

        return true;
    }

    public int executionCount() {
        return executed.size();
    }
}