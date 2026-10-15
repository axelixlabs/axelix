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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NullMarked;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.ProbeState.InstanceKey;
import com.axelixlabs.axelix.master.repository.InstanceRepository;

/**
 * JDBC-based implementation of {@link InstanceRegistry} that persists instances in a relational database.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Marsel Semenov
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
    public void reload(Instance instance) {
        jdbcAggregateTemplate.upsert(instance);
    }

    @Override
    public void reconcile(Map<InstanceKey, Instance> fresh, Set<InstanceKey> retained) {
        // The assumption is that every Instance without the heartbeat has come from auto-discovery
        List<InstanceId> toDelete = new ArrayList<>();
        Set<InstanceId> kept = new HashSet<>();
        List<Instance> instances = instanceRepository.findAllByLatestHeartBeatIsNull();
        for (Instance instance : instances) {
            if (isKept(instance, fresh, retained)) {
                kept.add(instance.id());
            } else {
                toDelete.add(instance.id());
            }
        }

        instanceRepository.deleteAllById(toDelete);

        // The same pod may be visible under several services, i.e. under several keys
        Map<InstanceId, Instance> toInsert = new LinkedHashMap<>();
        fresh.values().stream()
                .filter(instance -> !kept.contains(instance.id()))
                .forEach(instance -> toInsert.putIfAbsent(instance.id(), instance));

        jdbcAggregateTemplate.insertAll(toInsert.values());
    }

    private static boolean isKept(Instance instance, Map<InstanceKey, Instance> fresh, Set<InstanceKey> retained) {
        InstanceKey key = instance.discoveryKey();
        return key != null && !fresh.containsKey(key) && retained.contains(key);
    }

    @Override
    public void deRegister(InstanceId instanceId) {
        instanceRepository.deleteById(instanceId);
    }

    @Override
    public Optional<Instance> get(InstanceId instanceId) {
        return instanceRepository.findById(instanceId);
    }

    @Override
    public List<Instance> getAll() {
        return instanceRepository.findAll();
    }

    @Override
    public Set<Instance> findByQuery(String query) {
        return instanceRepository.findByNameLikeIgnoreCase("%" + query + "%");
    }
}
