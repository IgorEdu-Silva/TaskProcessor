package com.taskprocessor.support;

import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.model.TaskType;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class TakeRepositoryContractTest {
    protected abstract TaskRepositoryPort createRepository();

    @Test
    void shouldSaveAndFindTask() {
        var repo = createRepository();
        Task task = Task.create(TaskType.GENERATE_REPORT, "payload", Clock.systemUTC());
        repo.save(task);
        assertTrue(repo.findById(task.getId()).isPresent());
    }

    @Test
    void shouldReturnOnlyPendingTasks() {
        TaskRepositoryPort repo = createRepository();
        Task task = Task.create(TaskType.GENERATE_REPORT, "payload", Clock.systemUTC());
        repo.save(task);
        assertEquals(1, repo.findPendingTasks().size());
    }
}
