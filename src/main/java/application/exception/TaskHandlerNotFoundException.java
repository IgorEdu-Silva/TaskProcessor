package application.exception;

public class TaskHandlerNotFoundException extends ApplicationException {
    public TaskHandlerNotFoundException(String type) {
        super("HANDLER_NOT_FOUND", "No handler registered for task type" + type);
    };
}
