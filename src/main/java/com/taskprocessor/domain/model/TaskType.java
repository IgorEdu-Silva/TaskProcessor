package com.taskprocessor.domain.model;

import java.time.Duration;

public enum TaskType {
    GENERATE_REPORT(Duration.ofSeconds(10)),
    DATA_PROCESSING(Duration.ofMinutes(2));

    private final Duration timeout;

    TaskType(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration timeout() {
        return timeout;
    }
}
