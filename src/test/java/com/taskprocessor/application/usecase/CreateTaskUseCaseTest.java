package com.taskprocessor.application.usecase;

import com.taskprocessor.application.command.CreateTaskCommand;
import com.taskprocessor.application.factory.TaskFactory;
import com.taskprocessor.application.handler.TaskHandler;
import com.taskprocessor.application.port.TaskProcessor;
import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.application.registry.TaskHandlerRegistry;
import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.model.TaskStatus;
import com.taskprocessor.domain.model.TaskType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CreateTaskUseCaseTest {

    @Test
    void should_create_and_dispatch_task() {
        Clock clock = Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneOffset.UTC);
        TaskFactory factory = new TaskFactory(clock);
        var repository = mock(TaskRepositoryPort.class);
        var processor = mock(TaskProcessor.class);
        var useCase = new CreateTaskUseCase(repository, processor, factory);

        UUID id = useCase.execute(new CreateTaskCommand(TaskType.GENERATE_REPORT, "payload"));

        assertNotNull(id);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(repository).save(captor.capture());

        Task task = captor.getValue();
        assertEquals(id, task.getId());
        assertEquals(TaskStatus.PENDING, task.getStatus());

        verify(processor).enqueue(id);
    }

    @Test
    void should_fail_and_retry_when_handler_throws() {
        Clock clock = Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneOffset.UTC);
        var repository = mock(TaskRepositoryPort.class);
        var registry = mock(TaskHandlerRegistry.class);
        var handler = mock(TaskHandler.class);
        var task = Task.create(TaskType.GENERATE_REPORT, "payload", clock);

        task.requestProcessing();

        when(repository.markAsProcessing(task.getId())).thenReturn(true);
        when(repository.findById(task.getId())).thenReturn(Optional.of(task));
        when(registry.getHandler(task.getType())).thenReturn(handler);
        doThrow(new RuntimeException()).when(handler).execute(task);

        var useCase = new ProcessTaskUseCase(repository, registry);

        assertThrows(RuntimeException.class, () -> useCase.execute(task.getId()));
        assertEquals(TaskStatus.RETRY, task.getStatus());
        verify(repository).save(task);
    }

    @Test
    void should_not_process_when_task_is_in_final_state() {
        Clock clock = Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneOffset.UTC);
        var repository = mock(TaskRepositoryPort.class);
        var registry = mock(TaskHandlerRegistry.class);
        var task = Task.create(TaskType.GENERATE_REPORT, "payload", clock);

        task.requestProcessing();
        task.complete();

        when(repository.markAsProcessing(task.getId())).thenReturn(false);

        var useCase = new ProcessTaskUseCase(repository, registry);
        useCase.execute(task.getId());

        verify(repository, never()).save(any());
    }
}