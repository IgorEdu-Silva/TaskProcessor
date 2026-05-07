package com.taskprocessor.application.mapper;

import com.taskprocessor.application.dto.TaskResponse;
import com.taskprocessor.domain.model.Task;

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
