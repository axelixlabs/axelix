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
package com.axelixlabs.axelix.sbs.spring.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixBeansEndpointAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixCachesEndpointAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixConditionsEndpointAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixConfigurationsPropertiesEndpointAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixDetailsEndpointAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixEnvironmentEndpointAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixGcEndpointAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixHeapDumpEndpointAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixMetadataEndpointConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixMetricsAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.ContextRestarterAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.GitInformationProviderAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.JwtAuthAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.KafkaIntegrationAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.LibraryDiscovererAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.ProfileManagementAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.PropertyManagementAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.ScheduledTaskManagementAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.SelfRegistrationAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.ShortBuildInfoProviderAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.ThreadDumpManagementEndpointAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.TransactionMonitoringAutoConfiguration;

/**
 * Minimal Spring Boot application used exclusively for testing this application.
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 */
@SpringBootApplication(
        exclude = {
            AxelixBeansEndpointAutoConfiguration.class,
            AxelixCachesEndpointAutoConfiguration.class,
            AxelixConditionsEndpointAutoConfiguration.class,
            AxelixConfigurationsPropertiesEndpointAutoConfiguration.class,
            AxelixDetailsEndpointAutoConfiguration.class,
            AxelixEnvironmentEndpointAutoConfiguration.class,
            AxelixHeapDumpEndpointAutoConfiguration.class,
            AxelixMetadataEndpointConfiguration.class,
            AxelixMetricsAutoConfiguration.class,
            ContextRestarterAutoConfiguration.class,
            AxelixGcEndpointAutoConfiguration.class,
            GitInformationProviderAutoConfiguration.class,
            JwtAuthAutoConfiguration.class,
            KafkaIntegrationAutoConfiguration.class,
            LibraryDiscovererAutoConfiguration.class,
            ProfileManagementAutoConfiguration.class,
            PropertyManagementAutoConfiguration.class,
            SelfRegistrationAutoConfiguration.class,
            ScheduledTaskManagementAutoConfiguration.class,
            ShortBuildInfoProviderAutoConfiguration.class,
            ThreadDumpManagementEndpointAutoConfiguration.class,
            TransactionMonitoringAutoConfiguration.class
        })
@EnableCaching
@EnableFeignClients
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
