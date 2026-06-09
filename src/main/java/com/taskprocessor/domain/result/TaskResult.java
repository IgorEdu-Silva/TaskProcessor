package com.taskprocessor.domain.result;

public sealed interface TaskResult permits SuccessResult, FailureResult {

    static TaskResult success() {
        return new SuccessResult();
    }

    static TaskResult retryableFailure() {
        return new FailureResult(true);
    }

    static TaskResult permanentFailure() {
        return new FailureResult(false);
    }
}
