package com.taskprocessor.application.command;

import com.taskprocessor.domain.model.TaskType;

public record CreateTaskCommand(
        TaskType type,
        String payload
) {
}
