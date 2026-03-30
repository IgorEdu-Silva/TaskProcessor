package application.usecase;

import application.command.CreateTaskCommand;
import application.port.TaskProcessor;
import domain.model.Task;
import domain.repository.TaskRepository;

public class CreateTaskUseCase {
    private final TaskRepository repository;
    private final TaskProcessor processor;

    public CreateTaskUseCase(TaskRepository repository, TaskProcessor processor) {
        this.repository = repository;
        this.processor = processor;
    }

    public Long execute(CreateTaskCommand command){
        Task task = new Task(command.type(), command.payload());

        repository.save(task);

        processor.process(task.getId());

        return task.getId();
    }
}
