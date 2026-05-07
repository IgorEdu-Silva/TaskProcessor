package com.taskprocessor.contract;

import com.taskprocessor.application.port.TaskRepositoryPort;

abstract class TaskRepositoryContractTest {
    protected abstract TaskRepositoryPort createRepository();
}
