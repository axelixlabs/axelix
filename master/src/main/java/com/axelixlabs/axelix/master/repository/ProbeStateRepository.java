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

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import com.axelixlabs.axelix.master.domain.ProbeState;
import com.axelixlabs.axelix.master.domain.ProbeState.InstanceKey;

/**
 * Repository for {@link ProbeState}.
 *
 * @author Marsel Semenov
 */
public interface ProbeStateRepository extends ListCrudRepository<ProbeState, InstanceKey> {

    List<ProbeState> findAllByNextAttemptAtLessThanEqual(Instant now);

    /**
     * Leases all the states that are due, i.e. the ones {@link #findAllByNextAttemptAtLessThanEqual} returns.
     * Must be called under the {@link com.axelixlabs.axelix.master.service.discovery.DiscoveryLock}, so that
     * both calls see the same rows.
     */
    @Modifying
    @Query("""
            UPDATE probe_state
            SET next_attempt_at = :leaseUntil, version = version + 1
            WHERE next_attempt_at <= :now
            """)
    void leaseDue(@Param("now") Instant now, @Param("leaseUntil") Instant leaseUntil);
}
