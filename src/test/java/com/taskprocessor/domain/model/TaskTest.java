package com.taskprocessor.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    Clock clock = Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void shouldTransitionToProcessing() {
        Task task = Task.create(TaskType.GENERATE_REPORT, "payload", Clock.systemUTC());
        task.requestProcessing();
        assertEquals(TaskStatus.PROCESSING, task.getStatus());
    }

    @Test
    void shouldTransitionToDoneAfterComplete() {
        Task task = Task.create(TaskType.GENERATE_REPORT, "payload", Clock.systemUTC());
        task.requestProcessing();
        task.complete();
        assertEquals(TaskStatus.DONE, task.getStatus());
    }

    @Test
    void shouldTransitionToRetryAfterFirstFailure() {
        Task task = Task.create(TaskType.GENERATE_REPORT, "payload", Clock.systemUTC());
        task.requestProcessing();
        task.fail();
        assertEquals(TaskStatus.RETRY, task.getStatus());
    }

    @Test
    void shouldTransitionToErrorAfterMaxRetries() {
        Task task = Task.create(TaskType.GENERATE_REPORT, "payload", Clock.systemUTC());
        for (int i = 0; i < 3; i++) {
            task.requestProcessing();
            task.fail();
            if (task.getStatus() == TaskStatus.RETRY) {
                task.markForRetry();
            }
        }
        assertEquals(TaskStatus.ERROR, task.getStatus());
    }

    @Test
    void shouldThrowWhenInvalidTransition() {
        Task task = Task.create(TaskType.GENERATE_REPORT, "payload", Clock.systemUTC());
        assertThrows(IllegalStateException.class, task::complete);
    }

    @Test
    void shouldCreateTaskWithPendingStatus() {
        Task task = new Task(UUID.randomUUID(), TaskType.GENERATE_REPORT, "payload", clock);
        assertEquals(TaskStatus.PENDING, task.getStatus());
    }

    @Test
    void shouldStartProcessingWhenPeding() {
        Task task = new Task(UUID.randomUUID(), TaskType.GENERATE_REPORT, "payload", clock);
        task.requestProcessing();
        assertEquals(TaskStatus.PROCESSING, task.getStatus());
    }

    @Test
    void shouldMarkDoneWhenProcessing() {
        Task task = new Task(UUID.randomUUID(), TaskType.GENERATE_REPORT, "payload", clock);
        task.requestProcessing();
        task.complete();
        assertEquals(TaskStatus.DONE, task.getStatus());
    }

    @Test
    void shouldNotMarkDoneIfNotProcessing() {
        Task task = new Task(UUID.randomUUID(), TaskType.GENERATE_REPORT, "payload", clock);
        assertThrows(IllegalStateException.class, task::complete);
    }

    @Test
    void shouldNotAllowErrorAfterDone() {
        Task task = new Task(UUID.randomUUID(), TaskType.GENERATE_REPORT, "payload", clock);

        task.requestProcessing();
        task.complete();

        assertThrows(
                IllegalStateException.class,
                task::complete
        );
    }

    @Test
    void create() {
    }

    @Test
    void isProcessing() {
    }

    @Test
    void isFinished() {
    }

    @Test
    void isRetring() {
    }

    @Test
    void canStartProcessing() {
    }

    @Test
    void isPending() {
    }

    @Test
    void isError() {
    }

    @Test
    void getId() {
    }

    @Test
    void getType() {
    }

    @Test
    void getStatus() {
    }

    @Test
    void getPayload() {
    }
}

