package com.taskprocessor.infra.scheduler;

import com.taskprocessor.application.usecase.RecoverPendingTaskUseCase;
import com.taskprocessor.application.usecase.RetryTasksUseCase;
import com.taskprocessor.application.usecase.TimeoutTasksUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TaskCycleScheduler {

    private final RetryTasksUseCase retry;
    private final TimeoutTasksUseCase timeout;
    private final RecoverPendingTaskUseCase recover;

    public TaskCycleScheduler(
            RetryTasksUseCase retry,
            TimeoutTasksUseCase timeout,
            RecoverPendingTaskUseCase recover
    ) {
        this.retry = retry;
        this.timeout = timeout;
        this.recover = recover;
    }

    @Scheduled(fixedDelay = 5000)
    public void runCycle() {
        timeout.execute();
        recover.execute();
        retry.execute();
    }
}
