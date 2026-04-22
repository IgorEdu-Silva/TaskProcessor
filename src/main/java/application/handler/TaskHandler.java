package application.handler;

import domain.model.Task;

public interface TaskHandler {
    void execute(Task task);
}
