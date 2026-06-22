package com.taskprocessor.infra.processor;

import com.taskprocessor.application.usecase.ProcessTaskUseCase;
import com.taskprocessor.infra.async.QueueTaskProcessor;

public class AsyncTaskProcessor extends QueueTaskProcessor {

    private static final int DEFAULT_WORKERS = 16;
    private static final int DEFAULT_CAPACITY = 10_000;

    public AsyncTaskProcessor(ProcessTaskUseCase useCase) {
        this(useCase, DEFAULT_WORKERS, DEFAULT_CAPACITY);
    }

    public AsyncTaskProcessor(ProcessTaskUseCase useCase, int workers, int capacity) {
        super(useCase, workers, capacity);
    }
}
