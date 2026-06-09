package com.taskprocessor.domain.model;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Task(
        UUID id,
        TaskType type,
        String payload,
        TaskStatus status,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt,
        Instant nextRetryAt,
        int retryCount
) {

    public Task {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");

        if (retryCount < 0) {
            throw new IllegalArgumentException("retryCount must not be negative");
        }
    }

    public static Task create(TaskType type, String payload, Clock clock) {
        return new Task(
                UUID.randomUUID(),
                type,
                payload,
                TaskStatus.PENDING,
                Instant.now(clock),
                null,
                null,
                null,
                0
        );
    }

    public static Task rehydrate(
            UUID id,
            TaskType type,
            String payload,
            TaskStatus status,
            Instant createdAt,
            Instant startedAt,
            Instant finishedAt,
            int retryCount,
            Instant nextRetryAt
    ) {
        return new Task(
                id,
                type,
                payload,
                status,
                createdAt,
                startedAt,
                finishedAt,
                nextRetryAt,
                retryCount
        );
    }
}
