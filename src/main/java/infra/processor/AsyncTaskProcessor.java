package infra.processor;

import application.port.TaskProcessor;
import application.usecase.ProcessTaskUseCase;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class AsyncTaskProcessor implements TaskProcessor {

    private final ExecutorService executor;
    private final Semaphore limiter;
    private final ProcessTaskUseCase useCase;

    public AsyncTaskProcessor(ProcessTaskUseCase useCase, int maxConcurrency) {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.limiter = new Semaphore(maxConcurrency);
        this.useCase = useCase;
    }

    @Override
    public void enqueue(UUID taskId) {
        executor.submit(() -> executeWithLimit(taskId));
    }

    private void executeWithLimit(UUID taskId) {
        if (!limiter.tryAcquire()) {
            return;
        }

        try {
            useCase.execute(taskId);
        } finally {
            limiter.release();
        }
    }
}