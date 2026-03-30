package application.handler;

import domain.model.Task;
import domain.model.TaskType;

public interface TaskHandler {
    TaskType supports();
    void execute(Task task);
}
