package application.factory;

import domain.model.Task;
import domain.model.TaskType;

import java.time.Clock;

public class TaskFactory {

    private final Clock clock;

    public TaskFactory(Clock clock) {
        this.clock = clock;
    }

    public Task create(TaskType type, String payload) {
        return Task.create(type, payload, clock);
    }
}