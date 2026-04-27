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

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link SelfRegistrationInstanceEvictionScheduler}
 *
 * @author Nikita Kirillov
 */
@SpringBootTest(properties = "axelix.master.discovery.self.eviction.interval=60000000")
class SelfRegistrationInstanceEvictionSchedulerTest {

    @Autowired
    private InstanceRegistry instanceRegistry;

    @Autowired
    private SelfRegistrationInstanceEvictionScheduler selfRegistrationInstanceEvictionScheduler;

    @BeforeEach
    void setUp() {
        instanceRegistry.deRegisterAll(instanceRegistry.getAllIds());
    }

    @Test
    void shouldEvictInstance() {
        // given
        String instanceId = UUID.randomUUID().toString();
        Instant latestHeartBeat = Instant.now().minusSeconds(60000);
        registerInstance(instanceId, latestHeartBeat);

        // when
        selfRegistrationInstanceEvictionScheduler.evictStaleInstances();

        // then
        assertThat(instanceRegistry.get(InstanceId.of(instanceId))).isEmpty();
    }

    @Test
    void shouldNotEvictInstanceWithoutHeartbeat() {
        // given
        String staleInstanceId = UUID.randomUUID().toString();
        Instant staleHeartbeat = Instant.now().minusSeconds(60000);
        registerInstance(staleInstanceId, staleHeartbeat);

        String noHeartbeatInstanceId = UUID.randomUUID().toString();
        registerInstance(noHeartbeatInstanceId, null);

        // when
        selfRegistrationInstanceEvictionScheduler.evictStaleInstances();

        // then
        assertThat(instanceRegistry.get(InstanceId.of(staleInstanceId))).isEmpty();
        assertThat(instanceRegistry.get(InstanceId.of(noHeartbeatInstanceId))).isNotEmpty();
    }

    @Test
    void shouldEvictMultipleStaleInstances() {
        // given
        Set<String> staleIds = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            String id = UUID.randomUUID().toString();
            registerInstance(id, Instant.now().minusSeconds(60000));
            staleIds.add(id);
        }

        Set<String> freshIds = new HashSet<>();
        for (int i = 0; i < 2; i++) {
            String id = UUID.randomUUID().toString();
            registerInstance(id, Instant.now());
            freshIds.add(id);
        }

        // when
        selfRegistrationInstanceEvictionScheduler.evictStaleInstances();

        // then
        assertThat(staleIds).allSatisfy(instanceId -> assertThat(instanceRegistry.get(InstanceId.of(instanceId)))
                .isEmpty());
        assertThat(freshIds).allSatisfy(instanceId -> assertThat(instanceRegistry.get(InstanceId.of(instanceId)))
                .isNotEmpty());
    }

    private void registerInstance(String instanceId, Instant latestHeartBeat) {
        Instance instance = createInstance(instanceId, latestHeartBeat);
        instanceRegistry.register(instance);
    }
}
