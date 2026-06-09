package com.taskprocessor.domain.service;

import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.model.TaskStatus;
import com.taskprocessor.domain.model.TaskType;
import com.taskprocessor.domain.policy.RetryPolicy;
import com.taskprocessor.domain.result.TaskResult;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskLifecycleTest {

    private final Clock clock = Clock.fixed(
            Instant.parse("2024-01-01T10:00:00Z"),
            ZoneOffset.UTC
    );
    private final TaskLifecycle lifecycle = new TaskLifecycle(new RetryPolicy(), clock);

    @Test
    void shouldCreatePendingTask() {
        Task task = lifecycle.create(TaskType.GENERATE_REPORT, "payload");

        assertEquals(TaskStatus.PENDING, task.status());
        assertEquals(0, task.retryCount());
        assertEquals(Instant.parse("2024-01-01T10:00:00Z"), task.createdAt());
    }

    @Test
    void shouldStartPendingTask() {
        Task task = lifecycle.create(TaskType.GENERATE_REPORT, "payload");

        Task started = lifecycle.start(task);

        assertEquals(TaskStatus.PROCESSING, started.status());
        assertEquals(Instant.parse("2024-01-01T10:00:00Z"), started.startedAt());
    }

    @Test
    void shouldCompleteProcessingTask() {
        Task task = lifecycle.start(lifecycle.create(TaskType.GENERATE_REPORT, "payload"));

        Task completed = lifecycle.resolve(task, TaskResult.success());

        assertEquals(TaskStatus.DONE, completed.status());
        assertEquals(Instant.parse("2024-01-01T10:00:00Z"), completed.finishedAt());
    }

    @Test
    void shouldScheduleRetryAfterRetryableFailure() {
        Task task = lifecycle.start(lifecycle.create(TaskType.GENERATE_REPORT, "payload"));

        Task failed = lifecycle.resolve(task, TaskResult.retryableFailure());

        assertEquals(TaskStatus.RETRY, failed.status());
        assertEquals(1, failed.retryCount());
        assertEquals(Instant.parse("2024-01-01T10:00:04Z"), failed.nextRetryAt());
    }

    @Test
    void shouldFailPermanentlyAfterMaxRetries() {
        Task task = lifecycle.start(lifecycle.create(TaskType.GENERATE_REPORT, "payload"));

        Task firstRetry = lifecycle.markForRetry(lifecycle.fail(task));
        Task secondRetry = lifecycle.markForRetry(lifecycle.fail(lifecycle.start(firstRetry)));
        Task error = lifecycle.fail(lifecycle.start(secondRetry));

        assertEquals(TaskStatus.ERROR, error.status());
        assertEquals(3, error.retryCount());
    }

    @Test
    void shouldRejectInvalidTransition() {
        Task task = lifecycle.create(TaskType.GENERATE_REPORT, "payload");

        assertThrows(IllegalStateException.class, () -> lifecycle.complete(task));
    }
}
