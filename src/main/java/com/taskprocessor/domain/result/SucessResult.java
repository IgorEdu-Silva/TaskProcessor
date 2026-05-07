package com.taskprocessor.domain.result;

import com.taskprocessor.domain.model.TaskStatus;

public record SucessResult() implements TaskResult{
    @Override
    public TaskStatus resultingStatus() {
        return TaskStatus.DONE;
    }
}
