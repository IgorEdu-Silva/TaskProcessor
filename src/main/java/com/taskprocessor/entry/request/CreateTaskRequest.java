package com.taskprocessor.entry.request;

import com.taskprocessor.domain.model.TaskType;

public record CreateTaskRequest(
        TaskType type,
        String payload
) {
}
