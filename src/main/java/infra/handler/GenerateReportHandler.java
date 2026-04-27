package infra.handler;

import application.handler.TaskHandler;
import domain.model.Task;

public class GenerateReportHandler implements TaskHandler {

    @Override
    public boolean execute(Task task) {
        System.out.println("Generating report: " + task.getPayload());
        return true;
    }
}