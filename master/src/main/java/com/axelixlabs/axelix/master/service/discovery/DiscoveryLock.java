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
package com.axelixlabs.axelix.master.service.discovery;

import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lock shared by all Master replicas, held until the end of the current transaction. Implemented as an update
 * of a single row, since a row lock works the same way on every supported RDBMS.
 *
 * @author Marsel Semenov
 */
@Component
public class DiscoveryLock {

    private final NamedParameterJdbcOperations jdbcOperations;

    public DiscoveryLock(NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    /**
     * Blocks until no other replica holds the lock.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void acquire() {
        jdbcOperations.update("UPDATE discovery_lock SET version = version + 1 WHERE id = 1", Map.of());
    }
}
