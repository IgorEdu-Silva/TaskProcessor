package com.taskprocessor.domain.result;

public record FailureResult(boolean retryable) implements TaskResult {
}
