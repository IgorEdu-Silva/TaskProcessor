package application.result;

import domain.model.TaskStatus;

import java.util.UUID;

public record TaskExecutionResult(UUID id, TaskStatus status) {
}
