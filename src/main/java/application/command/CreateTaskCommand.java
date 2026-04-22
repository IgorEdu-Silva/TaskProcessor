package application.command;

import domain.model.TaskType;

public record CreateTaskCommand(
        TaskType type,
        String payload
) {
}
