package application.mapper;

import application.dto.TaskResponse;
import domain.model.Task;

public class TaskOutputMapper {
    private TaskOutputMapper(){

    }

    public static TaskResponse toResponse(Task task){
        return new TaskResponse(
            task.getId(),
            task.getType().name(),
            task.getStatus().name(),
            task.getPayload()
        );
    }
}
