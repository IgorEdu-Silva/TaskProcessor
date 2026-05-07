package com.taskprocessor.infra.processor;

import com.taskprocessor.application.port.TaskProcessor;
import com.taskprocessor.application.usecase.ProcessTaskUseCase;
import org.springframework.scheduling.annotation.Async;

import java.util.UUID;

public class AsyncTaskProcessor implements TaskProcessor {

    private final ProcessTaskUseCase useCase;

    public AsyncTaskProcessor(ProcessTaskUseCase useCase) {
        this.useCase = useCase;
    }

    @Async
    @Override
    public void enqueue(UUID taskId) {
        useCase.execute(taskId);
    }
}