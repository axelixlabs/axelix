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
package com.axelixlabs.axelix.master.domain;

import java.time.Instant;

import org.jspecify.annotations.Nullable;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * The append-only record of a single {@code @Scheduled} method invocation delivered by a managed instance.
 * Rows are never updated nor merged: duplicates are rejected by the UNIQUE index
 * {@code (instance_id, task_id, started_at)}.
 *
 * <p>Since the rows are never updated, the aggregate always reports itself as new
 * ({@link Persistable#isNew()} is {@code true}): therefore {@code save()} always issues an INSERT
 * and never an UPDATE.
 *
 * @param id              the surrogate identifier of this execution result.
 * @param groupId         the group id of the application (the G inside the GAV coordinate).
 * @param artifactId      the artifact id of the application (the A inside the GAV coordinate).
 * @param instanceId      the id of the instance that ran the task.
 * @param taskId          the id of the scheduled task, equal to the {@code toString()} of the proxied runnable.
 * @param startedAt       the timestamp when the task started its execution.
 * @param durationMillis  the duration of the task execution in milliseconds.
 * @param success         whether the task completed without throwing an exception.
 * @param errorType       the simple class name of the exception thrown by the task, if any.
 * @param errorMessage    the message of the exception thrown by the task, if any.
 *
 * @author Vyacheslav Yanin
 */
@Table("scheduled_task_execution_results")
public record ScheduledTaskExecutionResult(
        @Id String id,
        @Column("group_id") String groupId,
        @Column("artifact_id") String artifactId,
        @Column("instance_id") String instanceId,
        @Column("task_id") String taskId,
        @Column("started_at") Instant startedAt,
        @Column("duration_millis") long durationMillis,
        boolean success,
        @Column("error_type") @Nullable String errorType,
        @Column("error_message") @Nullable String errorMessage)
        implements Persistable<String> {

    @Override
    public String getId() {
        return id;
    }

    /**
     * The rows of this aggregate are append-only and are never updated, so the aggregate is always
     * considered new: {@code  *.save()} therefore always issues an INSERT and never an UPDATE.
     */
    @Override
    public boolean isNew() {
        return true;
    }
}
