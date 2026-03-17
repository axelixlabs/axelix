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
package com.axelixlabs.axelix.master.service.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.repository.InstanceRepository;
import com.axelixlabs.axelix.master.repository.entity.InstanceEntity;

/**
 * JDBC-based implementation of {@link InstanceRegistry} that persists instances in a relational database.
 *
 * @author Nikita Kirillov
 */
@Service
public class DatabaseInstanceRegistry implements InstanceRegistry {

    private final InstanceEntityMapper mapper;

    private final InstanceRepository instanceRepository;

    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public DatabaseInstanceRegistry(
            InstanceEntityMapper mapper,
            InstanceRepository instanceRepository,
            JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.mapper = mapper;
        this.instanceRepository = instanceRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
    }

    @Override
    public void register(Instance instance) {
        InstanceEntity entity = mapper.toEntity(instance);
        if (instanceRepository.existsById(entity.id())) {
            jdbcAggregateTemplate.update(entity);
        } else {
            jdbcAggregateTemplate.insert(entity);
        }
    }

    @Override
    public void registerAll(Collection<Instance> instances) {
        List<InstanceEntity> incoming = instances.stream().map(mapper::toEntity).toList();

        Map<String, InstanceEntity> existingById = new HashMap<>();
        instanceRepository.findAll().forEach(entity -> existingById.put(entity.id(), entity));

        List<InstanceEntity> toInsert = new ArrayList<>();
        List<InstanceEntity> toUpdate = new ArrayList<>();

        for (InstanceEntity entity : incoming) {
            InstanceEntity existing = existingById.get(entity.id());
            if (existing == null) {
                toInsert.add(entity);
            } else if (!existing.equals(entity)) {
                toUpdate.add(entity);
            }
        }

        if (!toInsert.isEmpty()) {
            jdbcAggregateTemplate.insertAll(toInsert);
        }
        if (!toUpdate.isEmpty()) {
            jdbcAggregateTemplate.updateAll(toUpdate);
        }
    }

    @Override
    public void deRegister(InstanceId instanceId) {
        instanceRepository.deleteById(instanceId.instanceId());
    }

    @Override
    public void deRegisterAll(Collection<InstanceId> instanceIds) {
        instanceRepository.deleteAllById(
                instanceIds.stream().map(InstanceId::instanceId).toList());
    }

    @Override
    public Optional<Instance> get(InstanceId instanceId) {
        return instanceRepository.findById(instanceId.instanceId()).map(mapper::toDomain);
    }

    @Override
    public @NonNull Set<Instance> getAll() {
        List<Instance> result = new ArrayList<>();
        instanceRepository.findAll().forEach(entity -> result.add(mapper.toDomain(entity)));
        return Set.copyOf(result);
    }

    @Override
    public @NonNull Set<InstanceId> getAllIds() {
        return instanceRepository.findAllIds().stream().map(InstanceId::of).collect(Collectors.toSet());
    }

    @Override
    public double getAverageHeapSize() {
        Double result = instanceRepository.findAverageHeap();
        return result != null ? result : -1d;
    }

    @Override
    public double getTotalHeapSize() {
        Double result = instanceRepository.findTotalHeap();
        return result != null ? result : 0d;
    }

    @Override
    public Set<Instance> findByQuery(String query) {
        return instanceRepository.findByNameLike("%" + query.toLowerCase() + "%").stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());
    }
}
