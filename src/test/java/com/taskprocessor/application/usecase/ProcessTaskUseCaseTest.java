package com.taskprocessor.application.usecase;

import com.taskprocessor.application.handler.TaskHandler;
import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.application.registry.TaskHandlerRegistry;
import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.model.TaskStatus;
import com.taskprocessor.domain.model.TaskType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;


import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProcessTaskUseCaseTest {
    @Mock TaskRepositoryPort repository;
    @Mock TaskHandlerRegistry registry;
    @Mock TaskHandler handler;

    @InjectMocks
    ProcessTaskUseCase useCase;

    @Test
    void shouldCompleteTaskWhenHandlerSucceeds() {
        UUID id = UUID.randomUUID();
        Task task = Task.create(TaskType.GENERATE_REPORT, "payload", Clock.systemUTC());
        task.requestProcessing();

        when(repository.markAsProcessing(id)).thenReturn(true);
        when(repository.findById(id)).thenReturn(Optional.of(task));
        when(registry.getHandler(any())).thenReturn(handler);
        when(handler.execute(task)).thenReturn(true);

        useCase.execute(id);

        assertEquals(TaskStatus.DONE, task.getStatus());
        verify(repository).save(task);
    }

    @Test
    void shouldFailTaskWhenHandlerThrows() {
        UUID id = UUID.randomUUID();
        Task task = Task.create(TaskType.GENERATE_REPORT, "payload", Clock.systemUTC());
        task.requestProcessing();

        when(repository.markAsProcessing(id)).thenReturn(true);
        when(repository.findById(id)).thenReturn(Optional.of(task));
        when(registry.getHandler(any())).thenReturn(handler);
        when(handler.execute(task)).thenThrow(new RuntimeException("erro"));

        assertThrows(RuntimeException.class, () -> useCase.execute(id));
        assertEquals(TaskStatus.RETRY, task.getStatus());
        verify(repository).save(task);
    }
}
