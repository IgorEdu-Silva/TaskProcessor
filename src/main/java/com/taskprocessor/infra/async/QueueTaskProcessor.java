package com.taskprocessor.infra.async;

import com.taskprocessor.application.port.TaskProcessor;
import com.taskprocessor.application.usecase.ProcessTaskUseCase;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

public class QueueTaskProcessor implements TaskProcessor {
    private final BlockingQueue<UUID> queue = new LinkedBlockingQueue<>();
    private final ExecutorService executor;
    private final ProcessTaskUseCase useCase;
    private volatile boolean running = true;

    public QueueTaskProcessor(ProcessTaskUseCase useCase, int workers) {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.useCase = useCase;

        startWorkers(workers);
    }

    private void startWorkers(int workers) {
        IntStream.range(0, workers)
                .forEach(i -> executor.submit(this::worker));
    }

    private void worker() {
        while (running) {
            try {
                UUID taskId = queue.take();
                useCase.execute(taskId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        running = false;
        executor.shutdown();
    }

    @Override
    public void enqueue(UUID taskId) {
        queue.offer(taskId);
    }
}
