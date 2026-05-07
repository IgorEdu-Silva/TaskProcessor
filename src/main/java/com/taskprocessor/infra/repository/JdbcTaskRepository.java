package com.taskprocessor.infra.repository;

import com.taskprocessor.application.port.TaskRepositoryPort;
import com.taskprocessor.domain.model.Task;
import com.taskprocessor.domain.model.TaskStatus;
import com.taskprocessor.domain.model.TaskType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.time.Clock;
import java.time.Instant;

import java.util.*;

@Repository
public class JdbcTaskRepository implements TaskRepositoryPort {

    private final JdbcTemplate jdbc;
    private final Clock clock;

    public JdbcTaskRepository(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public Task save(Task task) {
        jdbc.update("""
                INSERT INTO task (id, type, status, payload, retry_count, created_at, started_at, finished_at, next_retry_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    status = EXCLUDED.status,
                    retry_count = EXCLUDED.retry_count,
                    started_at = EXCLUDED.started_at,
                    finished_at = EXCLUDED.finished_at,
                    next_retry_at = EXCLUDED.next_retry_at
                """,
                task.getId(),
                task.getType().name(),
                task.getStatus().name(),
                task.getPayload(),
                task.getRetryCount(),
                Timestamp.from(task.getCreatedAt()),
                toTimestamp(task.getStartedAt()),
                toTimestamp(task.getFinishedAt()),
                toTimestamp(task.getNextRetryAt())
        );
        return task;
    }

    @Override
    public Optional<Task> findById(UUID id) {
        List<Task> result = jdbc.query(
                "SELECT * FROM task WHERE id = ?",
                (rs, rowNum) -> mapRow(rs),
                id
        );
        return result.stream().findFirst();
    }

    @Override
    public boolean markAsProcessing(UUID taskId) {
        int rows = jdbc.update("""
                UPDATE task
                SET status = 'PROCESSING', started_at = now()
                WHERE id = ?
                AND status IN ('PENDING', 'RETRY')
                """,
                taskId
        );
        return rows == 1;
    }

    @Override
    public List<Task> findPendingTasks() {
        return jdbc.query(
                "SELECT * FROM task WHERE status IN ('PENDING', 'RETRY')",
                (rs, rowNum) -> mapRow(rs)
        );
    }

    @Override
    public List<Task> findProcessingTasks() {
        return jdbc.query(
                "SELECT * FROM task WHERE status = 'PROCESSING'",
                (rs, rowNum) -> mapRow(rs)
        );
    }

    @Override
    public List<Task> findTasksInRetry() {
        return jdbc.query(
                "SELECT * FROM task WHERE status = 'RETRY' AND next_retry_at <= now()",
                (rs, rowNum) -> mapRow(rs)
        );
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        return Task.rehydrate(
                rs.getObject("id", UUID.class),
                TaskType.valueOf(rs.getString("type")),
                rs.getString("payload"),
                TaskStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toInstant(),
                toInstant(rs.getTimestamp("started_at")),
                toInstant(rs.getTimestamp("finished_at")),
                rs.getInt("retry_count"),
                toInstant(rs.getTimestamp("next_retry_at")),
                clock
        );
    }

    private Instant toInstant(Timestamp ts) {
        return ts != null ? ts.toInstant() : null;
    }

    private Timestamp toTimestamp(Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }
}
