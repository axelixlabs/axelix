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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NullMarked;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.repository.InstanceRepository;

/**
 * JDBC-based implementation of {@link InstanceRegistry} that persists instances in a relational database.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@Service
@NullMarked
@Transactional
public class DatabaseInstanceRegistry implements InstanceRegistry {

    // Technically, mixing the Repositories with JdbcAggregateTemplate abstraction layers is not
    // the brightest idea, but that is a trade-off for not making Instance implement Persistable and stuff.
    private final InstanceRepository instanceRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public DatabaseInstanceRegistry(
            InstanceRepository instanceRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.instanceRepository = instanceRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
    }

    @Override
    public void register(Instance instance) {
        if (instanceRepository.existsById(instance.id())) {
            jdbcAggregateTemplate.update(instance);
        } else {
            jdbcAggregateTemplate.insert(instance);
        }
    }

    @Override
    public void registerAll(Collection<Instance> instances) {
        jdbcAggregateTemplate.deleteAll(Instance.class);
        jdbcAggregateTemplate.insertAll(instances);
    }

    @Override
    public void deRegister(InstanceId instanceId) {
        instanceRepository.deleteById(instanceId);
    }

    @Override
    public void deRegisterAll(Collection<InstanceId> instanceIds) {
        instanceRepository.deleteAllById(instanceIds);
    }

    @Override
    public Optional<Instance> get(InstanceId instanceId) {
        return instanceRepository.findById(instanceId);
    }

    @Override
    public List<Instance> getAll() {
        // No need to coping collections or anything - the domain instances are not proxied
        return instanceRepository.findAll();
    }

    @Override
    public Set<InstanceId> getAllIds() {
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
        return instanceRepository.findByNameLikeIgnoreCase("%" + query + "%");
    }
}
