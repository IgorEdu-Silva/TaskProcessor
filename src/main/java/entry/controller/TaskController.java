package entry.controller;

import application.command.CreateTaskCommand;
import application.usecase.CreateTaskUseCase;

import java.util.UUID;

public class TaskController {

    private final CreateTaskUseCase useCase;

    public TaskController(CreateTaskUseCase useCase) {
        this.useCase = useCase;
    }

    public UUID create(CreateTaskCommand command) {
        return useCase.execute(command);
    }
}