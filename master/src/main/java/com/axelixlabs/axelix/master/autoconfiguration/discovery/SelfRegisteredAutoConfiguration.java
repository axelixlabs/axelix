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
package com.axelixlabs.axelix.master.autoconfiguration.discovery;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.master.service.MemoryUsageCache;
import com.axelixlabs.axelix.master.service.discovery.DefaultInstanceFactory;
import com.axelixlabs.axelix.master.service.discovery.DefaultManagementInstance;
import com.axelixlabs.axelix.master.service.discovery.InstanceFactory;
import com.axelixlabs.axelix.master.service.discovery.ManagementInstance;
import com.axelixlabs.axelix.master.service.discovery.selfregistered.SelfRegisteredServiceDiscoverer;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * Auto-configuration for self-registered related components.
 *
 * @author Sergey Cherkasov
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "axelix.master.discovery", name = "auto", havingValue = "false")
public class SelfRegisteredAutoConfiguration {

    @Bean
    public ManagementInstance managementInstance(InstanceRegistry instanceRegistry, MemoryUsageCache memoryUsageCache) {
        return new DefaultManagementInstance(instanceRegistry, memoryUsageCache);
    }

    @Bean
    public InstanceFactory instanceFactory() {
        return new DefaultInstanceFactory();
    }

    @Bean
    public SelfRegisteredServiceDiscoverer managementSelfRegisteredService(
            ManagementInstance managementInstance, InstanceFactory instanceFactory) {
        return new SelfRegisteredServiceDiscoverer(managementInstance, instanceFactory);
    }
}
