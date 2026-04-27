package domain.model;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Task {

    private static final int MAX_RETRIES = 3;

    private final UUID id;
    private final TaskType type;
    private final String payload;
    private final Instant createdAt;
    private final Clock clock;

    private TaskStatus status;
    private Instant startedAt;
    private Instant finishedAt;
    private Instant nextRetryAt;
    private int retryCount;

    public Task(UUID id, TaskType type, String payload, Clock clock) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.payload = Objects.requireNonNull(payload);
        this.clock = Objects.requireNonNull(clock);

        this.status = TaskStatus.PENDING;
        this.createdAt = Instant.now(clock);
        this.retryCount = 0;
    }

    public static Task create(TaskType type, String payload, Clock clock) {
        return new Task(UUID.randomUUID(), type, payload, clock);
    }

    public void requestProcessing() {
        if (!canStartProcessing()) {
            throw new IllegalStateException("Task cannot start processing from status " + status);
        }

        status = TaskStatus.PROCESSING;
        startedAt = Instant.now(clock);
    }

    public void complete() {
        ensureStatus(TaskStatus.PROCESSING);

        status = TaskStatus.DONE;
        finishedAt = Instant.now(clock);
    }

    public void fail() {
        ensureStatus(TaskStatus.PROCESSING);

        retryCount++;

        if (retryCount >= MAX_RETRIES) {
            status = TaskStatus.ERROR;
            finishedAt = Instant.now(clock);
            return;
        }

        status = TaskStatus.RETRY;
        nextRetryAt = Instant.now(clock).plusSeconds(retryBackoff());
    }

    private long retryBackoff() {
        return (long) Math.pow(4, retryCount);
    }

    public void markForRetry() {
        ensureStatus(TaskStatus.RETRY);
        status = TaskStatus.PENDING;
        nextRetryAt = null;
    }

    public boolean canRetry() {
        return retryCount < MAX_RETRIES;
    }

    public boolean canStartProcessing() {
        return status == TaskStatus.PENDING || status == TaskStatus.RETRY;
    }

    public boolean isTimedOut(Duration timeout) {
        return status == TaskStatus.PROCESSING
                && startedAt != null
                && startedAt.plus(timeout).isBefore(Instant.now(clock));
    }

    public boolean isFinalState() {
        return status == TaskStatus.DONE || status == TaskStatus.ERROR;
    }

    public Duration processingTime() {
        if (startedAt == null) return Duration.ZERO;

        Instant end = finishedAt != null ? finishedAt : Instant.now(clock);
        return Duration.between(startedAt, end);
    }

    private void ensureStatus(TaskStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Invalid state transition: " + status + " expected " + expected
            );
        }
    }

    public UUID getId() { return id; }
    public TaskType getType() { return type; }
    public TaskStatus getStatus() { return status; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public int getRetryCount() { return retryCount; }
    public Instant getNextRetryAt() { return nextRetryAt; }
}