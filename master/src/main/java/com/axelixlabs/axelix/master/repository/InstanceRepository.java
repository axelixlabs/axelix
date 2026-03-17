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

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.axelixlabs.axelix.master.repository.entity.InstanceEntity;

/**
 * Repository for {@link InstanceEntity}.
 *
 * @author Nikita Kirillov
 */
public interface InstanceRepository extends CrudRepository<InstanceEntity, String> {

    @Query("SELECT AVG(heap) FROM instances")
    Double findAverageHeap();

    @Query("SELECT SUM(heap) FROM instances")
    Double findTotalHeap();

    @Query("SELECT id FROM instances")
    List<String> findAllIds();

    @Query("""
        SELECT * FROM instances
        WHERE LOWER(name) LIKE :query
        """)
    List<InstanceEntity> findByNameLike(@Param("query") String query);
}
