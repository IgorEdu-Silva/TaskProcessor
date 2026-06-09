package com.taskprocessor.domain.policy;

import java.time.Duration;

public class RetryPolicy {
    private static final int MAX_RETRIES = 3;

    public boolean canRetry(int retryCount) {
        return retryCount < MAX_RETRIES;
    }

    public Duration backoffFor(int retryCount) {
        return Duration.ofSeconds((long) Math.pow(4, retryCount));
    }
}
