package com.taskprocessor.application.result;

import com.taskprocessor.domain.model.TaskStatus;

import java.util.UUID;

public record TaskExecutionResult(UUID id, TaskStatus status) {
}
