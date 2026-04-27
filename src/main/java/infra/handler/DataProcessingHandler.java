package infra.handler;

import application.handler.TaskHandler;
import domain.model.Task;

public class DataProcessingHandler implements TaskHandler {

    @Override
    public boolean execute(Task task) {
        System.out.println("Processing data: " + task.getPayload());
        return true;
    }
}