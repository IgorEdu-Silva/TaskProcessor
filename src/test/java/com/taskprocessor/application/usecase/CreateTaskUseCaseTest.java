package com.taskprocessor.application.usecase;

import com.taskprocessor.application.command.CreateTaskCommand;
import com.taskprocessor.application.port.TaskDispatchResult;
import com.taskprocessor.application.port.TaskProcessor;
import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.model.TaskStatus;
import com.taskprocessor.domain.model.TaskType;
import com.taskprocessor.domain.policy.RetryPolicy;
import com.taskprocessor.domain.service.TaskLifecycle;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateTaskUseCaseTest {

    @Test
    void shouldCreateAndDispatchTask() {
        Clock clock = Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneOffset.UTC);
        TaskLifecycle lifecycle = new TaskLifecycle(new RetryPolicy(), clock);
        var repository = mock(TaskRepositoryPort.class);
        var processor = mock(TaskProcessor.class);
        var useCase = new CreateTaskUseCase(repository, processor, lifecycle);
        when(processor.enqueue(any()))
                .thenReturn(TaskDispatchResult.ACCEPTED);

        var id = useCase.execute(new CreateTaskCommand(TaskType.GENERATE_REPORT, "payload"));

        assertNotNull(id);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(repository).save(captor.capture());

        Task task = captor.getValue();
        assertEquals(id, task.id());
        assertEquals(TaskStatus.PENDING, task.status());
        assertEquals(Instant.parse("2024-01-01T10:00:00Z"), task.createdAt());

        verify(processor).enqueue(id);
    }
}
