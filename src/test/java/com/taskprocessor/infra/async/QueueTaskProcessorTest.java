package com.taskprocessor.infra.async;

import com.taskprocessor.application.command.CreateTaskCommand;
import com.taskprocessor.application.handler.TestHandlerTest;
import com.taskprocessor.application.port.TaskDispatchResult;
import com.taskprocessor.application.registry.TaskHandlerRegistry;
import com.taskprocessor.application.usecase.CreateTaskUseCase;
import com.taskprocessor.application.usecase.ProcessTaskUseCase;
import com.taskprocessor.domain.model.TaskStatus;
import com.taskprocessor.domain.model.TaskType;
import com.taskprocessor.domain.policy.RetryPolicy;
import com.taskprocessor.domain.service.TaskLifecycle;
import com.taskprocessor.support.InMemoryTaskRepositoryTest;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QueueTaskProcessorTest {

    @Test
    void shouldProcessQueuedTask() {
        var useCase = mock(ProcessTaskUseCase.class);
        var taskId = UUID.randomUUID();

        try (var processor = new QueueTaskProcessor(useCase, 1, 10)) {
            assertEquals(TaskDispatchResult.ACCEPTED, processor.enqueue(taskId));

            awaitUntil(() -> {
                try {
                    verify(useCase).execute(taskId);
                    return true;
                } catch (AssertionError ignored) {
                    return false;
                }
            }, Duration.ofSeconds(3));
        }
    }

    @Test
    void shouldReturnRejectedWhenQueueIsFull() throws Exception {
        var useCase = mock(ProcessTaskUseCase.class);
        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);

        try (var processor = new QueueTaskProcessor(useCase, 1, 1)) {
            doAnswer(invocation -> {
                started.countDown();
                release.await();
                return null;
            }).when(useCase).execute(any());

            assertEquals(TaskDispatchResult.ACCEPTED, processor.enqueue(UUID.randomUUID()));
            assertTrue(started.await(2, TimeUnit.SECONDS));
            assertEquals(TaskDispatchResult.ACCEPTED, processor.enqueue(UUID.randomUUID()));
            assertEquals(TaskDispatchResult.REJECTED, processor.enqueue(UUID.randomUUID()));

            release.countDown();
        }
    }

    @Test
    void shouldReturnStoppedAfterClose() {
        var useCase = mock(ProcessTaskUseCase.class);
        var processor = new QueueTaskProcessor(useCase, 1, 10);

        processor.close();

        assertEquals(TaskDispatchResult.STOPPED, processor.enqueue(UUID.randomUUID()));
    }

    @Test
    void shouldDrainQueueBeforeClosing() {
        var executed = new AtomicInteger();
        var useCase = mock(ProcessTaskUseCase.class);

        doAnswer(invocation -> {
            executed.incrementAndGet();
            return null;
        }).when(useCase).execute(any());

        var processor = new QueueTaskProcessor(useCase, 1, 10);

        assertEquals(TaskDispatchResult.ACCEPTED, processor.enqueue(UUID.randomUUID()));
        assertEquals(TaskDispatchResult.ACCEPTED, processor.enqueue(UUID.randomUUID()));
        assertEquals(TaskDispatchResult.ACCEPTED, processor.enqueue(UUID.randomUUID()));

        processor.close();

        assertEquals(3, executed.get());
    }

    private static void awaitUntil(BooleanSupplier condition, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Interrupted while waiting");
            }
        }

        fail("Condition was not met within " + timeout);
    }

    @FunctionalInterface
    private interface BooleanSupplier {
        boolean getAsBoolean();
    }

    @Test
    void shouldAcceptConcurrentEnqueue() throws Exception {

        int totalTasks = 1000;

        var executed = new AtomicInteger();

        var useCase = mock(ProcessTaskUseCase.class);

        doAnswer(invocation -> {
            executed.incrementAndGet();
            return null;
        }).when(useCase).execute(any());

        try (var processor =
                     new QueueTaskProcessor(useCase, 10, 2000);
             var executor =
                     Executors.newVirtualThreadPerTaskExecutor()) {

            var futures = IntStream.range(0, totalTasks)
                    .mapToObj(i -> executor.submit(() -> processor.enqueue(UUID.randomUUID())))
                    .toList();

            for (Future<TaskDispatchResult> future : futures) {
                assertEquals(TaskDispatchResult.ACCEPTED, future.get());
            }

            executor.shutdown();
            assertTrue(
                    executor.awaitTermination(5, TimeUnit.SECONDS)
            );

            awaitUntil(
                    () -> executed.get() == totalTasks,
                    Duration.ofSeconds(10)
            );

            assertEquals(totalTasks, executed.get());
        }
    }

    @Test
    void shouldExposeProcessorCounters() throws Exception {
        var useCase = mock(ProcessTaskUseCase.class);
        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);

        try (var processor = new QueueTaskProcessor(useCase, 1, 1)) {
            doAnswer(invocation -> {
                started.countDown();
                release.await();
                return null;
            }).when(useCase).execute(any());

            assertEquals(TaskDispatchResult.ACCEPTED, processor.enqueue(UUID.randomUUID()));
            assertTrue(started.await(2, TimeUnit.SECONDS));
            assertEquals(TaskDispatchResult.ACCEPTED, processor.enqueue(UUID.randomUUID()));
            assertEquals(TaskDispatchResult.REJECTED, processor.enqueue(UUID.randomUUID()));

            assertEquals(2, processor.acceptedTasks());
            assertEquals(1, processor.rejectedTasks());

            release.countDown();
        }
    }

    @Test
    void shouldAcceptConcurrentEnqueueWithDiscardedReturnValue() throws Exception {

        int totalTasks = 1000;

        var executed = new AtomicInteger();

        var useCase = mock(ProcessTaskUseCase.class);

        doAnswer(invocation -> {
            executed.incrementAndGet();
            return null;
        }).when(useCase).execute(any());

        try (var processor =
                     new QueueTaskProcessor(useCase, 10, 2000);
             var executor =
                     Executors.newVirtualThreadPerTaskExecutor()) {

            IntStream.range(0, totalTasks)
                    .forEach(i ->
                            executor.submit(() ->
                                    processor.enqueue(UUID.randomUUID())
                            )
                    );

            executor.shutdown();
            assertTrue(
                    executor.awaitTermination(5, TimeUnit.SECONDS)
            );

            awaitUntil(
                    () -> executed.get() == totalTasks,
                    Duration.ofSeconds(10)
            );

            assertEquals(totalTasks, executed.get());
        }
    }

    @Test
    void shouldProcessTasksInParallel() throws Exception {

        int workers = 4;

        var started = new CountDownLatch(workers);
        var release = new CountDownLatch(1);

        var useCase = mock(ProcessTaskUseCase.class);

        doAnswer(invocation -> {
            started.countDown();
            release.await();
            return null;
        }).when(useCase).execute(any());

        try (var processor =
                     new QueueTaskProcessor(useCase, workers, 100)) {

            IntStream.range(0, workers)
                    .forEach(i ->
                            assertEquals(TaskDispatchResult.ACCEPTED, processor.enqueue(UUID.randomUUID()))
                    );

            assertTrue(
                    started.await(2, TimeUnit.SECONDS),
                    "Workers did not start in parallel"
            );

            verify(useCase, timeout(2000).times(workers))
                    .execute(any());

            release.countDown();
        }
    }

    @Test
    void shouldProcessTaskOnlyOnceWhenEnqueuedMultipleTimes() throws Exception {

        var repository = new InMemoryTaskRepositoryTest(Clock.systemUTC());

        var lifecycle =
                new TaskLifecycle(
                        new RetryPolicy(),
                        Clock.systemUTC()
                );

        var handler = new TestHandlerTest();

        var registry = new TaskHandlerRegistry(
                Map.of(
                        TaskType.GENERATE_REPORT,
                        handler
                )
        );

        var processUseCase =
                new ProcessTaskUseCase(
                        repository,
                        registry,
                        lifecycle
                );

        UUID taskId;

        try (var processor =
                     new QueueTaskProcessor(processUseCase, 10, 1000)) {

            var createUseCase =
                    new CreateTaskUseCase(
                            repository,
                            processor,
                            lifecycle
                    );

            taskId = createUseCase.execute(
                    new CreateTaskCommand(
                            TaskType.GENERATE_REPORT,
                            "report"
                    )
            );

            IntStream.range(0, 100)
                    .forEach(i ->
                            processor.enqueue(taskId)
                    );

            awaitUntil(
                    () -> repository.findById(taskId)
                            .map(task -> task.status() == TaskStatus.DONE)
                            .orElse(false),
                    Duration.ofSeconds(5)
            );
        }

        assertEquals(1, handler.executionCount());
    }
}
