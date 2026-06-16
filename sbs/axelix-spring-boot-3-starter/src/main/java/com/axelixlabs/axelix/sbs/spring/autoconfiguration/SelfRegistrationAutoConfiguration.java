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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.sbs.spring.core.config.SelfRegistrationConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.log.SLF4JLogger;
import com.axelixlabs.axelix.sbs.spring.core.master.BuildInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultBuildInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultSelfRegistrationMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.SelfRegistrationLifecycleListener;
import com.axelixlabs.axelix.sbs.spring.core.master.SelfRegistrationMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.SelfRegistrationService;
import com.axelixlabs.axelix.sbs.spring.core.master.ServiceMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.validate.ValidationListener;

/**
 * Auto-configuration for instance self-registration.
 *
 * @since 04.02.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "axelix.sbs.discovery", value = "auto", havingValue = "true")
public class SelfRegistrationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ValidationListener validationListener() {
        return new ValidationListener();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "axelix.sbs.discovery")
    public SelfRegistrationConfigurationProperties selfRegistrationConfigurationProperties() {
        return new SelfRegistrationConfigurationProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public BuildInfoProvider buildInfoProvider(ObjectProvider<BuildProperties> buildPropertiesProvider) {
        return new DefaultBuildInfoProvider(buildPropertiesProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public SelfRegistrationMetadataAssembler selfRegistrationMetadataAssembler(
            ServiceMetadataAssembler serviceMetadataAssembler,
            SelfRegistrationConfigurationProperties selfRegistrationConfigurationProperties,
            BuildInfoProvider buildInfoProvider) {
        return new DefaultSelfRegistrationMetadataAssembler(
                serviceMetadataAssembler, selfRegistrationConfigurationProperties, buildInfoProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public SelfRegistrationService selfRegistrationService(
            SelfRegistrationConfigurationProperties properties,
            ObjectMapper objectMapper,
            SelfRegistrationMetadataAssembler selfRegistrationMetadataAssembler,
            JwtEncoderService jwtEncoderService) {
        return new SelfRegistrationService(
                new SLF4JLogger(LoggerFactory.getLogger(SelfRegistrationService.class)),
                objectMapper::writeValueAsString,
                properties,
                selfRegistrationMetadataAssembler,
                jwtEncoderService);
    }

    @Bean
    @ConditionalOnMissingBean
    public SelfRegistrationLifecycleListener selfRegistrationLifecycleListener(
            SelfRegistrationService selfRegistrationService) {
        return new SelfRegistrationLifecycleListener(selfRegistrationService);
    }
}
