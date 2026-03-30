package application.mapper;

import application.command.CreateTaskCommand;
import domain.model.Task;

public class TaskInputMapper {
    public static Task toDomain(CreateTaskCommand cmd){
        return new Task(cmd.type(), cmd.payload());
    }
}