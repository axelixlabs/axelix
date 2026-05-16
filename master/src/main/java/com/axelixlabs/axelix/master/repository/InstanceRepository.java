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
import java.util.List;
import java.util.Set;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;

/**
 * Repository for {@link Instance} aggregate.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public interface InstanceRepository extends ListCrudRepository<Instance, InstanceId> {

    @Query("SELECT AVG(heap) FROM instances")
    Double findAverageHeap();

    @Query("SELECT SUM(heap) FROM instances")
    Double findTotalHeap();

    Set<Instance> findByNameLikeIgnoreCase(@Param("query") String query);

    @Modifying
    @Query("DELETE FROM instances WHERE latest_heart_beat IS NOT NULL AND latest_heart_beat < :threshold")
    int deleteWhereHeartbeatOlderThan(@Param("threshold") Instant threshold);

    @Modifying
    @Query("DELETE FROM instances WHERE latest_heart_beat IS NULL")
    void deleteAllWithoutHeartbeat();
}
