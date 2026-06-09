package com.taskprocessor.application.usecase;

import com.taskprocessor.application.handler.TaskHandler;
import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.application.registry.TaskHandlerRegistry;
import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.model.TaskStatus;
import com.taskprocessor.domain.model.TaskType;
import com.taskprocessor.domain.policy.RetryPolicy;
import com.taskprocessor.domain.result.TaskResult;
import com.taskprocessor.domain.service.TaskLifecycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessTaskUseCaseTest {

    private final Clock clock = Clock.fixed(
            Instant.parse("2024-01-01T10:00:00Z"),
            ZoneOffset.UTC
    );
    private final TaskLifecycle lifecycle = new TaskLifecycle(new RetryPolicy(), clock);

    @Mock TaskRepositoryPort repository;
    @Mock TaskHandlerRegistry registry;
    @Mock TaskHandler handler;

    ProcessTaskUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessTaskUseCase(repository, registry, lifecycle);
    }

    @Test
    void shouldCompleteTaskWhenHandlerSucceeds() {
        Task task = lifecycle.create(TaskType.GENERATE_REPORT, "payload");
        Task started = lifecycle.start(task);

        when(repository.findById(task.id())).thenReturn(Optional.of(task));
        when(repository.saveWhenStatus(started, TaskStatus.PENDING)).thenReturn(true);
        when(registry.getHandler(task.type())).thenReturn(handler);
        when(handler.execute(started)).thenReturn(TaskResult.success());

        useCase.execute(task.id());

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(repository).saveWhenStatus(captor.capture(), eq(TaskStatus.PROCESSING));

        assertEquals(TaskStatus.DONE, captor.getValue().status());
    }

    @Test
    void shouldFailTaskWhenHandlerThrows() {
        Task task = lifecycle.create(TaskType.GENERATE_REPORT, "payload");
        Task started = lifecycle.start(task);

        when(repository.findById(task.id())).thenReturn(Optional.of(task));
        when(repository.saveWhenStatus(started, TaskStatus.PENDING)).thenReturn(true);
        when(registry.getHandler(task.type())).thenReturn(handler);
        when(handler.execute(started)).thenThrow(new RuntimeException("erro"));

        assertThrows(RuntimeException.class, () -> useCase.execute(task.id()));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(repository).saveWhenStatus(captor.capture(), eq(TaskStatus.PROCESSING));

        assertEquals(TaskStatus.RETRY, captor.getValue().status());
    }

    @Test
    void shouldNotProcessWhenClaimFails() {
        Task task = lifecycle.create(TaskType.GENERATE_REPORT, "payload");
        Task started = lifecycle.start(task);

        when(repository.findById(task.id())).thenReturn(Optional.of(task));
        when(repository.saveWhenStatus(started, TaskStatus.PENDING)).thenReturn(false);

        useCase.execute(task.id());

        verify(registry, never()).getHandler(any());
        verify(repository, never()).save(any());
    }
}
