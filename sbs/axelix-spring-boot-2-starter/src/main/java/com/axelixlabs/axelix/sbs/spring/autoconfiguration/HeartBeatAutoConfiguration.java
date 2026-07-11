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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.sbs.spring.core.config.HeartBeatConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.log.SLF4JLogger;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultHeartBeatMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.HeartBeatLifecycleIgnitor;
import com.axelixlabs.axelix.sbs.spring.core.master.HeartBeatMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.HeartBeatService;
import com.axelixlabs.axelix.sbs.spring.core.master.ServiceMetadataAssembler;

/**
 * Autoconfiguration for heart-beating of the Instance.
 *
 * @since 04.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@AutoConfiguration(after = ValidationListenerAutoConfiguration.class)
@ConditionalOnProperty(value = "axelix.sbs.discovery.auto", havingValue = "true")
public class HeartBeatAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = HeartBeatConfigurationProperties.CONFIG_PROPS_PREFIX)
    public HeartBeatConfigurationProperties heartBeatConfigurationProperties() {
        return new HeartBeatConfigurationProperties();
    }

    @Bean
    public HeartBeatMetadataAssembler heartBeatMetadataAssembler(
            ServiceMetadataAssembler serviceMetadataAssembler,
            HeartBeatConfigurationProperties heartBeatConfigurationProperties) {

        return new DefaultHeartBeatMetadataAssembler(serviceMetadataAssembler, heartBeatConfigurationProperties);
    }

    @Bean
    public HeartBeatService heartBeatService(
            HeartBeatConfigurationProperties properties,
            ObjectMapper objectMapper,
            HeartBeatMetadataAssembler heartBeatMetadataAssembler,
            JwtEncoderService jwtEncoderService) {
        return new HeartBeatService(
                new SLF4JLogger(LoggerFactory.getLogger(HeartBeatService.class)),
                objectMapper::writeValueAsString,
                properties,
                heartBeatMetadataAssembler,
                jwtEncoderService);
    }

    @Bean
    public HeartBeatLifecycleIgnitor heartBeatLifecycleIgnitor(HeartBeatService heartBeatService) {
        return new HeartBeatLifecycleIgnitor(heartBeatService);
    }
}
