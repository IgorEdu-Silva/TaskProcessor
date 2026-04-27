package infra.config;

import application.factory.TaskFactory;
import application.port.TaskProcessor;
import application.port.TaskRepositoryPort;
import application.registry.TaskHandlerRegistry;
import application.usecase.*;
import domain.model.TaskType;
import infra.handler.*;
import infra.processor.AsyncTaskProcessor;
import infra.repository.InMemoryTaskRepository;

import java.time.Clock;
import java.util.Map;

public class AppConfig {
    private final TaskRepositoryPort repository = new InMemoryTaskRepository();

    public TaskRepositoryPort repository() {
        return repository;
    }

    public TaskHandlerRegistry registry() {
        return new TaskHandlerRegistry(Map.of(
                TaskType.GENERATE_REPORT, new GenerateReportHandler(),
                TaskType.DATA_PROCESSING, new DataProcessingHandler()
        ));
    }

    public ProcessTaskUseCase processUseCase() {
        return new ProcessTaskUseCase(repository(), registry());
    }

    public TaskProcessor processor() {
        return new AsyncTaskProcessor(processUseCase(), 10);
    }

    public CreateTaskUseCase createUseCase() {
        return new CreateTaskUseCase(
                repository(),
                processor(),
                new TaskFactory(Clock.systemUTC())
        );
    }
}