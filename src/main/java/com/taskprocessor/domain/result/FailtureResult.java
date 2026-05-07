package com.taskprocessor.domain.result;

import com.taskprocessor.domain.model.TaskStatus;

public record FailtureResult(boolean retryable) implements TaskResult {
    @Override
    public TaskStatus resultingStatus(){
        return retryable ? TaskStatus.RETRY : TaskStatus.ERROR;
    }
}
