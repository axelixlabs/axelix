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
import java.util.List;
import java.util.Set;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;

import static com.axelixlabs.axelix.master.domain.Instance.InstanceStatus;

/**
 * Repository for {@link Instance} aggregate.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public interface InstanceRepository extends ListCrudRepository<Instance, InstanceId> {

    Set<Instance> findByNameLikeIgnoreCase(@Param("query") String query);

    @Query("""
        SELECT siblings.*
        FROM instances siblings
        JOIN instances target
            ON siblings.group_id = target.group_id
            AND siblings.artifact_id = target.artifact_id
        WHERE target.instance_id = :instanceId
        """)
    List<Instance> findSiblings(@Param("instanceId") String instanceId);

    @Query("""
        SELECT COUNT(*)
        FROM (
            SELECT group_id, artifact_id
            FROM instances
            WHERE instance_id IN (:instanceIds)
            GROUP BY group_id, artifact_id
        ) instances_by_application
        """)
    int countDistinctApplications(@Param("instanceIds") Collection<String> instanceIds);

    @Modifying
    @Query("DELETE FROM instances WHERE latest_heart_beat IS NOT NULL AND latest_heart_beat < :threshold")
    int deleteWhereHeartbeatOlderThan(@Param("threshold") Instant threshold);

    @Modifying
    @Query("DELETE FROM instances WHERE latest_heart_beat IS NULL")
    void deleteAllWithoutHeartbeat();

    @Modifying
    @Query("""
        UPDATE instances
        SET
            latest_heart_beat = :heartBeat,
            status = :status,
            heap = :heap
        WHERE
            instance_id = :instanceId
        """)
    void refreshInstanceStatus(
            @Param("instanceId") String instanceId,
            @Param("status") InstanceStatus status,
            @Param("heap") double heap,
            @Param("heartBeat") Instant heartBeat);
}
