package com.taskprocessor.support;

import com.taskprocessor.application.port.TaskProcessor;

import java.util.UUID;

public class FakeTaskProcessorTest implements TaskProcessor {
    @Override
    public void enqueue(UUID id){

    }
}
