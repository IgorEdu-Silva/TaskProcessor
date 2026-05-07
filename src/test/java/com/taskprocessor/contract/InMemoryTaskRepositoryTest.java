package com.taskprocessor.contract;

import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.infra.repository.InMemoryTaskRepository;

class InMemoryTaskRepositoryTest extends TaskRepositoryContractTest {
    @Override
    protected TaskRepositoryPort createRepository() {
        return new InMemoryTaskRepository();
    }
}
