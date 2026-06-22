package com.taskprocessor.support;

import com.taskprocessor.application.port.TaskDispatchResult;
import com.taskprocessor.application.port.TaskProcessor;

import java.util.UUID;

public class FakeTaskProcessorTest implements TaskProcessor {
    @Override
    public TaskDispatchResult enqueue(UUID id){
        return TaskDispatchResult.ACCEPTED;
    }
}
