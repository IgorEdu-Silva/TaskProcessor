package com.taskprocessor.application.mapper;

import com.taskprocessor.application.dto.TaskResponse;
import com.taskprocessor.domain.model.Task;

public class TaskOutputMapper {
    private TaskOutputMapper(){

    }

    public static TaskResponse toResponse(Task task){
        return new TaskResponse(
            task.id(),
            task.type().name(),
            task.status().name(),
            task.payload()
        );
    }
}
