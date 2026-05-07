package com.taskprocessor.infra.scheduler;

import com.taskprocessor.application.usecase.RetryTasksUseCase;
import com.taskprocessor.application.usecase.TimeoutTasksUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TaskCycleScheduler {

    private final RetryTasksUseCase retry;
    private final TimeoutTasksUseCase timeout;

    public TaskCycleScheduler(RetryTasksUseCase retry, TimeoutTasksUseCase timeout) {
        this.retry = retry;
        this.timeout = timeout;
    }

    @Scheduled(fixedDelay = 5000)
    public void runCycle() {
        retry.execute();
        timeout.execute();
    }
}