package com.taskprocessor.application.handler;

import com.taskprocessor.domain.model.Task;

public class SlowHandlerTest implements TaskHandler {

    @Override
    public boolean execute(Task task) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}

        return true;
    }
}