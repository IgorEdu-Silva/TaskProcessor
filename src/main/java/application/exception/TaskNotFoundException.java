package application.exception;

public class TaskNotFoundException extends ApplicationException {
    public TaskNotFoundException(Long id) {
        super("TASK_NOT_FOUND", "Task" + id + " not found");
    }
}
