package infra.scheduler;

import application.usecase.RetryTasksUseCase;
import application.usecase.TimeoutTasksUseCase;

public class TaskScheduler {

    private final RetryTasksUseCase retry;
    private final TimeoutTasksUseCase timeout;

    public TaskScheduler(RetryTasksUseCase retry, TimeoutTasksUseCase timeout) {
        this.retry = retry;
        this.timeout = timeout;
    }

    public void runCycle() {
        retry.execute();
        timeout.execute();
    }
}