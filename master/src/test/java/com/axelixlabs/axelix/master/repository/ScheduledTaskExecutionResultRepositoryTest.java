/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.repository;

import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.UncategorizedSQLException;

import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for the {@link ScheduledTaskExecutionResultRepository} that verify both the
 * Liquibase migration and the entity mapping on every supported {@code OssRdbms}.
 *
 * @author Vyacheslav Yanin
 */
@DatabaseMatrixTest
class ScheduledTaskExecutionResultRepositoryTest {

    @Autowired
    private ScheduledTaskExecutionResultRepository subject;

    @BeforeEach
    @AfterEach
    void cleanup() {
        subject.deleteAll();
    }

    @Test
    void save_shouldPersistAndReadBackScheduledTaskExecutionResult() {
        var id = UUID.randomUUID();
        var startedAt = Instant.parse("2026-09-27T10:00:00.123Z");
        var executionResult = new ScheduledTaskExecutionResult(
                id,
                "com.axelixlabs",
                "sample-app",
                "instance-1",
                "com.example.Job#run()",
                startedAt,
                1500L,
                false,
                "NullPointerException",
                "boom");
        // A second execution of the same instance and task within the same second must be
        // distinguishable from the first one by its millisecond timestamp.
        var sameSecondExecution = new ScheduledTaskExecutionResult(
                UUID.randomUUID(),
                "com.axelixlabs",
                "sample-app",
                "instance-1",
                "com.example.Job#run()",
                startedAt.plusMillis(1),
                2000L,
                true,
                null,
                null);

        subject.save(executionResult);
        subject.save(sameSecondExecution);

        ScheduledTaskExecutionResult loaded = subject.findById(id).orElseThrow();
        assertThat(loaded).isEqualTo(executionResult);
        ScheduledTaskExecutionResult sameSecondLoaded = subject.findById(sameSecondExecution.id()).orElseThrow();
        assertThat(sameSecondLoaded).isEqualTo(sameSecondExecution);
    }

    @Test
    void save_shouldRejectDuplicateInstanceTaskStartedAt() {
        var first = new ScheduledTaskExecutionResult(
                UUID.randomUUID(),
                "com.axelixlabs",
                "sample-app",
                "instance-1",
                "com.example.Job#run()",
                Instant.parse("2026-09-27T10:00:00Z"),
                100L,
                true,
                null,
                null);
        subject.save(first);
        var duplicate = new ScheduledTaskExecutionResult(
                UUID.randomUUID(),
                "com.axelixlabs",
                "sample-app",
                "instance-1",
                "com.example.Job#run()",
                Instant.parse("2026-09-27T10:00:00Z"),
                200L,
                true,
                null,
                null);

        assertThatThrownBy(() -> saveExpectingIntegrityViolation(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * <p>This method exists because different databases report the same constraint violation
     * differently: PostgreSQL and MySQL throw {@link DataIntegrityViolationException}, whereas
     * SQLite throws {@link UncategorizedSQLException} wrapping a {@link SQLException}
     * with the error code {@code 19} ({@code SQLITE_CONSTRAINT}). The method normalizes the
     * SQLite case so that the caller can always rely on {@link DataIntegrityViolationException}.
     *
     * @throws DataIntegrityViolationException when the insert is rejected by the UNIQUE constraint.
     */
    private void saveExpectingIntegrityViolation(ScheduledTaskExecutionResult result) {
        try {
            subject.save(result);
        } catch (UncategorizedSQLException e) {
            // SQLite: code 19 = SQLITE_CONSTRAINT
            if (e.getCause() instanceof SQLException sqlEx && sqlEx.getErrorCode() == 19) {
                throw new DataIntegrityViolationException(e.getMessage(), e);
            }
            throw e;
        }
    }
}
