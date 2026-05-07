package com.taskprocessor.application.load;

import com.taskprocessor.application.command.CreateTaskCommand;
import com.taskprocessor.application.factory.TaskFactory;
import com.taskprocessor.application.handler.TestHandlerTest;
import com.taskprocessor.application.registry.TaskHandlerRegistry;
import com.taskprocessor.application.usecase.CreateTaskUseCase;
import com.taskprocessor.application.usecase.ProcessTaskUseCase;
import com.taskprocessor.domain.model.TaskStatus;
import com.taskprocessor.domain.model.TaskType;
import com.taskprocessor.infra.async.QueueTaskProcessor;
import org.junit.jupiter.api.Test;
import com.taskprocessor.support.InMemoryTaskRepositoryTest;

import java.time.Clock;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskLoadTest {
    @Test
    void loadTest_shouldNotLoseOrDuplicateTasks() throws Exception {

        int totalTasks = 10000;

        var repository = new InMemoryTaskRepositoryTest(Clock.systemUTC());

        var handler = new TestHandlerTest();

        var registry = new TaskHandlerRegistry(Map.of(
                TaskType.GENERATE_REPORT, handler
        ));

        var processUseCase = new ProcessTaskUseCase(repository, registry);

        var processor = new QueueTaskProcessor(processUseCase, 10);

        var createUseCase = new CreateTaskUseCase(
                repository,
                processor,
                new TaskFactory(Clock.systemUTC())
        );

        // 🔥 carga
        IntStream.range(0, totalTasks)
                .forEach(i -> {
                    var id = createUseCase.execute(
                            new CreateTaskCommand(TaskType.GENERATE_REPORT, "task-" + i)
                    );

                    // 🔥 spam de duplicação
                    IntStream.range(0, 5)
                            .forEach(j -> processor.enqueue(id));
                });

        // ⏳ espera
        while (repository.findAll().stream()
                .anyMatch(t -> t.getStatus() != TaskStatus.DONE)) {
            Thread.sleep(50);
        }

        var allTasks = repository.findAll();

        long done = allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();

        assertEquals(totalTasks, done);

        assertEquals(totalTasks, handler.executionCount());
    }
}
