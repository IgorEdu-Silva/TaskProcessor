package com.taskprocessor.infra.async;

import com.taskprocessor.application.port.TaskDispatchResult;
import com.taskprocessor.application.port.TaskProcessor;
import com.taskprocessor.application.usecase.ProcessTaskUseCase;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class QueueTaskProcessor implements TaskProcessor, AutoCloseable {
    private static final System.Logger LOGGER = System.getLogger(QueueTaskProcessor.class.getName());
    private static final Duration DEFAULT_SHUTDOWN_TIMEOUT = Duration.ofSeconds(10);

    private final BlockingQueue<UUID> queue;
    private final ExecutorService executor;
    private final ProcessTaskUseCase useCase;
    private final Semaphore concurrency;
    private final Thread dispatcher;
    private final Duration shutdownTimeout;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicInteger activeTasks = new AtomicInteger();
    private final LongAdder acceptedTasks = new LongAdder();
    private final LongAdder rejectedTasks = new LongAdder();
    private final LongAdder completedTasks = new LongAdder();
    private final LongAdder failedTasks = new LongAdder();

    public QueueTaskProcessor(ProcessTaskUseCase useCase, int maxConcurrency) {
        this(useCase, maxConcurrency, 10_000);
    }

    public QueueTaskProcessor(ProcessTaskUseCase useCase, int maxConcurrency, int capacity) {
        this(useCase, maxConcurrency, capacity, DEFAULT_SHUTDOWN_TIMEOUT);
    }

    public QueueTaskProcessor(
            ProcessTaskUseCase useCase,
            int maxConcurrency,
            int capacity,
            Duration shutdownTimeout
    ) {
        if (maxConcurrency < 1) {
            throw new IllegalArgumentException("maxConcurrency must be greater than zero");
        }

        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be greater than zero");
        }

        this.useCase = Objects.requireNonNull(useCase, "useCase must not be null");
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.concurrency = new Semaphore(maxConcurrency, true);
        this.shutdownTimeout = Objects.requireNonNull(shutdownTimeout, "shutdownTimeout must not be null");
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.dispatcher = Thread.ofVirtual()
                .name("task-dispatcher")
                .start(this::dispatch);
    }

    @Override
    public TaskDispatchResult enqueue(UUID taskId) {
        Objects.requireNonNull(taskId, "taskId must not be null");

        if (!running.get()) {
            return TaskDispatchResult.STOPPED;
        }

        if (!queue.offer(taskId)) {
            rejectedTasks.increment();
            return TaskDispatchResult.REJECTED;
        }

        acceptedTasks.increment();
        return TaskDispatchResult.ACCEPTED;
    }

    private void dispatch() {
        while (running.get() || !queue.isEmpty()) {
            try {
                UUID taskId = queue.poll(100, TimeUnit.MILLISECONDS);
                if (taskId != null) {
                    concurrency.acquire();
                    executor.submit(() -> process(taskId));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.ERROR, "Task dispatch failed", e);
            }
        }
    }

    private void process(UUID taskId) {
        activeTasks.incrementAndGet();

        try {
            useCase.execute(taskId);
            completedTasks.increment();
        } catch (Exception e) {
            failedTasks.increment();
            LOGGER.log(System.Logger.Level.ERROR, "Task processing failed", e);
        } finally {
            activeTasks.decrementAndGet();
            concurrency.release();
        }
    }

    public int queuedTasks() {
        return queue.size();
    }

    public int activeTasks() {
        return activeTasks.get();
    }

    public long acceptedTasks() {
        return acceptedTasks.sum();
    }

    public long rejectedTasks() {
        return rejectedTasks.sum();
    }

    public long completedTasks() {
        return completedTasks.sum();
    }

    public long failedTasks() {
        return failedTasks.sum();
    }

    public void shutdown() {
        close();
    }

    @Override
    public void close() {
        if (!running.getAndSet(false)) {
            return;
        }

        waitForDispatcher();
        executor.shutdown();
        waitForExecutor();
    }

    private void waitForDispatcher() {
        try {
            dispatcher.join(shutdownTimeout.toMillis());
            if (dispatcher.isAlive()) {
                dispatcher.interrupt();
                dispatcher.join(shutdownTimeout.toMillis());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            dispatcher.interrupt();
        }
    }

    private void waitForExecutor() {
        try {
            if (!executor.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
