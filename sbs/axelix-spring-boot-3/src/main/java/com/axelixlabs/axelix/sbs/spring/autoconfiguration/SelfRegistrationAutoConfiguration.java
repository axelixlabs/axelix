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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.core.config.SelfRegistrationConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultSelfRegistrationHttpClient;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultSelfRegistrationMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.SelfRegistrationHttpClient;
import com.axelixlabs.axelix.sbs.spring.core.master.SelfRegistrationMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.SelfRegistrationService;
import com.axelixlabs.axelix.sbs.spring.core.master.ServiceMetadataAssembler;

/**
 * Auto-configuration for instance self-registration.
 *
 * @since 04.02.2026
 * @author Nikita Kirillov
 */
@AutoConfiguration(after = AxelixMetadataEndpointConfiguration.class)
@EnableConfigurationProperties(SelfRegistrationConfigurationProperties.class)
@ConditionalOnProperty(value = "axelix.sbs.discovery.auto", havingValue = "true")
public class SelfRegistrationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SelfRegistrationHttpClient selfRegistrationHttpClient() {
        return new DefaultSelfRegistrationHttpClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public SelfRegistrationMetadataAssembler selfRegistrationMetadataAssembler(
            ServiceMetadataAssembler serviceMetadataAssembler,
            SelfRegistrationConfigurationProperties selfRegistrationConfigurationProperties) {
        return new DefaultSelfRegistrationMetadataAssembler(
                serviceMetadataAssembler, selfRegistrationConfigurationProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SelfRegistrationService selfRegistrationService(
            SelfRegistrationConfigurationProperties properties,
            SelfRegistrationMetadataAssembler selfRegistrationMetadataAssembler,
            DefaultSelfRegistrationHttpClient selfRegistrationHttpClient) {
        return new SelfRegistrationService(properties, selfRegistrationMetadataAssembler, selfRegistrationHttpClient);
    }
}
