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

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;

/**
 * Repository for the {@link ScheduledTaskExecutionResult} aggregate.
 *
 * @author Vyacheslav Yanin
 * @author Nikita Kirillov
 */
@Repository
public interface ScheduledTaskExecutionResultRepository
        extends ListCrudRepository<ScheduledTaskExecutionResult, String> {

    /**
     * @return the ids out of the given ones that are already stored.
     */
    @Query("SELECT id FROM scheduled_task_execution_results WHERE id IN (:ids)")
    Set<String> findStoredIds(Collection<String> ids);

    /**
     * @return the number of the deleted executions.
     */
    @Modifying
    @Query("DELETE FROM scheduled_task_execution_results WHERE started_at < :threshold")
    int deleteStartedBefore(@Param("threshold") Instant threshold);

    /**
     * Deletes all the executions of every task of every application except the latest ones.
     *
     * @param maxExecutionsPerTask the number of the latest executions of each task to keep.
     * @return the number of the deleted executions.
     */
    @Modifying
    @Query("""
        DELETE FROM scheduled_task_execution_results
        WHERE id IN (
            SELECT id FROM (
                SELECT id,
                       ROW_NUMBER() OVER (
                           PARTITION BY group_id, artifact_id, task_id ORDER BY started_at DESC, id DESC) AS position
                FROM scheduled_task_execution_results
            ) ranked
            WHERE position > :maxExecutionsPerTask
        )
        """)
    int deleteAllButLatest(@Param("maxExecutionsPerTask") int maxExecutionsPerTask);
}
