package com.taskprocessor.application.handler;

import com.taskprocessor.domain.model.Task;

import java.util.concurrent.atomic.AtomicInteger;

public class FlakyHandlerTest implements TaskHandler {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public boolean execute(Task task) {
        if (counter.incrementAndGet() < 3) {
            throw new RuntimeException("fail");
        }
        return true;
    }
}