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
package com.axelixlabs.axelix.master.service;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InMemoryInstanceRegistry;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link InstanceRegistrar}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DefaultInstanceRegistrarTest.DefaultInstanceManagerTestConfiguration.class)
public class DefaultInstanceRegistrarTest {

    @Autowired
    private InstanceRegistrar instanceRegistrar;

    @Autowired
    private MemoryUsageCache memoryUsageCache;

    @Autowired
    private InstanceRegistry instanceRegistry;

    @TestConfiguration
    static class DefaultInstanceManagerTestConfiguration {

        @Bean
        public InstanceRegistry instanceRegistry() {
            return new InMemoryInstanceRegistry();
        }

        @Bean
        public InstanceRegistrar instanceManager(InstanceRegistry instanceRegistry, MemoryUsageCache memoryUsageCache) {
            return new DefaultInstanceRegistrar(instanceRegistry, memoryUsageCache);
        }

        @Bean
        public MemoryUsageCache memoryUsageCache() {
            return new InMemoryMemoryUsageCache();
        }
    }

    @Test
    void shouldRegisterInstances() {
        String instanceId = UUID.randomUUID().toString();
        Instance instance = TestObjectFactory.createInstance(instanceId);

        // when.
        instanceRegistrar.register(instance);

        // then.
        assertThat(instanceRegistry.get(InstanceId.of(instanceId))).isNotEmpty();
        assertThat(memoryUsageCache.getHeapSize(InstanceId.of(instanceId))).isNotNull();
    }

    @Test
    void shouldDeregister() {
        String instanceId = UUID.randomUUID().toString();
        Instance instance = TestObjectFactory.createInstance(instanceId);
        instanceRegistrar.register(instance);

        // when.
        instanceRegistrar.deregister(InstanceId.of(instanceId));

        // then.
        assertThat(instanceRegistry.get(InstanceId.of(instanceId))).isEmpty();
        assertThat(memoryUsageCache.getHeapSize(InstanceId.of(instanceId))).isEqualTo(-1.0);
    }
}
