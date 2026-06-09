package com.taskprocessor.domain.service;

import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.model.TaskStatus;
import com.taskprocessor.domain.model.TaskType;
import com.taskprocessor.domain.policy.RetryPolicy;
import com.taskprocessor.domain.result.FailureResult;
import com.taskprocessor.domain.result.SuccessResult;
import com.taskprocessor.domain.result.TaskResult;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

public class TaskLifecycle {

    private final RetryPolicy retryPolicy;
    private final Clock clock;

    public TaskLifecycle(RetryPolicy retryPolicy, Clock clock) {
        this.retryPolicy = retryPolicy;
        this.clock = clock;
    }

    public Task create(TaskType type, String payload) {
        return Task.create(type, payload, clock);
    }

    public Task start(Task task) {
        requireStatus(task, TaskStatus.PENDING);

        return new Task(
                task.id(),
                task.type(),
                task.payload(),
                TaskStatus.PROCESSING,
                task.createdAt(),
                now(),
                null,
                null,
                task.retryCount()
        );
    }

    public Task resolve(Task task, TaskResult result) {
        return switch (result) {
            case SuccessResult ignored -> complete(task);
            case FailureResult failure -> fail(task, failure.retryable());
        };
    }

    public Task complete(Task task) {
        requireStatus(task, TaskStatus.PROCESSING);

        return new Task(
                task.id(),
                task.type(),
                task.payload(),
                TaskStatus.DONE,
                task.createdAt(),
                task.startedAt(),
                now(),
                null,
                task.retryCount()
        );
    }

    public Task fail(Task task) {
        return fail(task, true);
    }

    public Task fail(Task task, boolean retryable) {
        requireStatus(task, TaskStatus.PROCESSING);

        int retries = task.retryCount() + 1;
        if (!retryable || !retryPolicy.canRetry(retries)) {
            return new Task(
                    task.id(),
                    task.type(),
                    task.payload(),
                    TaskStatus.ERROR,
                    task.createdAt(),
                    task.startedAt(),
                    now(),
                    null,
                    retries
            );
        }

        return new Task(
                task.id(),
                task.type(),
                task.payload(),
                TaskStatus.RETRY,
                task.createdAt(),
                task.startedAt(),
                null,
                now().plus(retryPolicy.backoffFor(retries)),
                retries
        );
    }

    public Task timeout(Task task) {
        return fail(task);
    }

    public Task markForRetry(Task task) {
        requireStatus(task, TaskStatus.RETRY);

        return new Task(
                task.id(),
                task.type(),
                task.payload(),
                TaskStatus.PENDING,
                task.createdAt(),
                task.startedAt(),
                null,
                null,
                task.retryCount()
        );
    }

    public boolean canStartProcessing(Task task) {
        return task.status() == TaskStatus.PENDING;
    }

    public boolean isFinalState(Task task) {
        return task.status() == TaskStatus.DONE
                || task.status() == TaskStatus.ERROR;
    }

    public boolean isRetryDue(Task task) {
        return task.status() == TaskStatus.RETRY
                && task.nextRetryAt() != null
                && !task.nextRetryAt().isAfter(now());
    }

    public boolean isTimedOut(Task task, Duration timeout) {
        return task.status() == TaskStatus.PROCESSING
                && processingTime(task).compareTo(timeout) > 0;
    }

    public Duration processingTime(Task task) {
        if (task.startedAt() == null) {
            return Duration.ZERO;
        }

        Instant end = task.finishedAt() != null
                ? task.finishedAt()
                : now();

        return Duration.between(task.startedAt(), end);
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private void requireStatus(Task task, TaskStatus... expected) {
        boolean valid = Arrays.stream(expected)
                .anyMatch(status -> task.status() == status);

        if (!valid) {
            throw new IllegalStateException("Invalid task state: " + task.status());
        }
    }
}
