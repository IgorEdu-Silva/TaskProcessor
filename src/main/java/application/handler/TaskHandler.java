package application.handler;

import domain.model.Task;

public interface TaskHandler {
    boolean execute(Task task);
}
