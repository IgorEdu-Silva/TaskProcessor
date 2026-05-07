package com.taskprocessor.domain.result;

import com.taskprocessor.domain.model.TaskStatus;

public record PendingResult() implements TaskResult {
    @Override
    public TaskStatus resultingStatus(){
        return TaskStatus.PENDING;
    }
}
